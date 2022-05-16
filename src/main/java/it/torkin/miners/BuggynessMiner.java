package it.torkin.miners;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.jgit.revwalk.RevCommit;

import it.torkin.dao.git.GitDao;
import it.torkin.dao.git.UnableToAccessRepositoryException;
import it.torkin.dao.git.UnableToGetChangeSetException;
import it.torkin.dao.git.UnableToGetCommitsException;
import it.torkin.dao.jira.JiraDao;
import it.torkin.dao.jira.JiraIssue;
import it.torkin.dao.jira.JiraRelease;
import it.torkin.dao.jira.UnableToGetAllFixedBugsException;
import it.torkin.dao.jira.UnableToGetReleasesException;
import it.torkin.dao.jira.UnknownJiraReleaseException;

public class BuggynessMiner extends Miner {

    private final String jiraProject;

    /** proportion */
    private double estimatedP;
    private static final double ALPHA = 0.5;    // 1/2, may be suboptimal. Choose negative powers of 2 to improve perfomance

    private Logger logger = Logger.getLogger(this.getClass().getName());

    public BuggynessMiner(String owner, String project) {
        super(owner, project);
        this.jiraProject = project.toUpperCase();
        this.estimatedP = 0;
    }

    public String getJiraProject() {
        return jiraProject;
    }

    private double estimateIvIndex(int fvIndex, int ovIndex){

        return Math.floor(Math.max(0, fvIndex - (fvIndex - ovIndex) * estimatedP));
    }

    /**
     * Updates average proportion value from sample
     * @param sample
     */
    private void updateEstimatedP(double sample){
        estimatedP = ((1 - ALPHA) * estimatedP) + (ALPHA * sample);
    }

    private double calculateP(int ovIndex, int fvIndex, int ivIndex){
        return (double) (fvIndex - ivIndex) / (fvIndex - ovIndex);
    }
    
    @Override
    public void mine(MineDataBean bean) throws UnableToMineDataException {

        
        JiraRelease fv;
        JiraRelease iv;
        JiraRelease[] fixingVersions;
        JiraRelease[] affectedVersions;
        RevCommit fixCommit;
        Set<String> changeSet;
        JiraDao jiraDao;
        GitDao gitDao;
        JiraRelease ov;
        List<JiraIssue> fixedBugs;
        
        // statistics on issues
        int withFvIssues = 0;
        int nonProductionIssues = 0;
        int noFixCommitIssues = 0;
        int knownIvIssues = 0;
        int estimatedIvIssues = 0;
        
        // get all fixed bug issues in jira s.t lastRelease.date < issue.fixedVersion[0].date < release.date
        try {
            jiraDao = new JiraDao(this.jiraProject);
            gitDao = new GitDao(this.repo);
            fixedBugs = jiraDao.getTimeOrderedFixedBugIssues(new Date(0), bean.getTimeOrderedReleases().get(bean.getReleaseIndex()).getReleaseDate());

            estimatedP = 1;  // starting value of p set to 1 based on assumption that IV equals OV when no P is available to estimate IV

            withFvIssues = fixedBugs.size();
            
            for (JiraIssue fixedBug : fixedBugs) {

                ov = jiraDao.getAllReleased(fixedBug.getFields().getCreated(), new Date()).get(0);
                
                // get latest commit older than fixedVersion s.t. commit.date < issue.fixedVersion.date and commit.comment.contains(issue.key)
                fixingVersions = fixedBug.getFields().getFixVersions();
                fv = fixingVersions[fixingVersions.length - 1]; // will use only most recent fixing version
                    
                if (fv.getName().compareTo(ov.getName()) != 0){ // discards non-production defects
                        fixCommit = gitDao.getLatestCommit(fv.getReleaseDate(), fixedBug.getKey()); // assuming that the fix commit is the most recent one before fv release date which includes JIRA bug id in it's description
                        
                        if (fixCommit != null) { // some silly dev could have forgot to include jira issue key in fixing commit
                            changeSet = gitDao.getCommitChangeSet(fixCommit);
                            
                            for (String changed : changeSet) {
                                affectedVersions = fixedBug.getFields().getVersions();
                                
                                if (affectedVersions.length == 0 || affectedVersions[0].getReleaseDate() == null || affectedVersions[0].getReleaseDate().compareTo(ov.getReleaseDate()) > 0) {    // not null and concistency check on IV
    
                                    // estimate injectedVersion using proportion
                                    iv = bean.getTimeOrderedReleases().get((int) estimateIvIndex(jiraDao.getReleaseOrderIndex(fv), jiraDao.getReleaseOrderIndex(ov)));
                                    estimatedIvIssues ++;
                                }
                                else {
                                    // sets IV as the earliest release among affected ones and updates proportion estimate 
                                    iv = affectedVersions[0];
                                    updateEstimatedP(calculateP(
                                        jiraDao.getReleaseOrderIndex(ov),
                                        jiraDao.getReleaseOrderIndex(fv),
                                        jiraDao.getReleaseOrderIndex(iv)
                                    ));  
                                    knownIvIssues ++;                      
                                }
                                
                                // at this point we have a defined IV
                                for (JiraRelease av : jiraDao.getAllReleased(iv.getReleaseDate(), fv.getReleaseDate())) {   // better not rely entirely on AVs listed in JIRA, as they are often wrong.
                                    Map<Feature, String> measure = bean.getObservationMatrix().getMatrix().get(av.getName()).get(changed);
                                    if (measure != null) { // changed file may be absent in current av
                                        measure.put(Feature.BUGGYNESS, "yes");
                                    }
                                }
                                
                            }
    
                        } else { noFixCommitIssues ++; }
                    } else { nonProductionIssues++; }

            }
            String msg = String.format("withFvIssues: %d, nonProductionIssues: %d, noFixCommitIssues: %d, knownIvIssues: %d, estimatedIvIssues: %d, ", withFvIssues, nonProductionIssues, noFixCommitIssues, knownIvIssues, estimatedIvIssues);
            logger.info(msg);
        } catch (UnableToGetAllFixedBugsException | UnableToAccessRepositoryException | UnableToGetCommitsException
                | UnableToGetChangeSetException | UnableToGetReleasesException | UnknownJiraReleaseException e) {
            throw new UnableToMineBuggynessException(e);
        }

    }

}
