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
import it.torkin.dao.jira.UnableToGetAllFixedBugsException;
import it.torkin.dao.jira.UnableToGetReleasesException;
import it.torkin.dao.jira.UnknownJiraReleaseException;
import it.torkin.entities.Release;

public class BuggynessMiner extends Miner {

    private final String jiraProject;

    /** proportion */
    private double estimatedP;
    private static final double ALPHA = 0.5;    // 1/2, may be suboptimal. Choose negative powers of 2 to improve perfomance

    // statistics
    private int knownIvIssues = 0;
    private int estimatedIvIssues = 0;


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
    
    private void putBuggyness(JiraDao jiraDao, Release iv, Release fv, MineDataBean bean, String changed) throws UnableToGetReleasesException {
        // at this point we have a defined IV
        for (Release av : jiraDao.getAllReleased(iv.getReleaseDate(), fv.getReleaseDate())) { // better not rely entirely on AVs listed in JIRA issue, as they are often wrong.
            Map<String, Map<Feature, String>> observations = bean.getObservationMatrix().getMatrix().get(av.getName());
            if (observations != null){   // av could be not in desired range of releases to measure
                Map<Feature, String> observation = observations.get(changed);
                if (observation != null) { // changed file may be absent in current av
                    observation.put(Feature.BUGGYNESS, "yes");
                }
            }
        }
    }

    private Release estimateIv(JiraIssue fixedBug, MineDataBean bean, JiraDao jiraDao, Release ov, Release fv ) throws UnableToGetReleasesException, UnknownJiraReleaseException{
        
        Release iv;
        Release[] affectedVersions = fixedBug.getFields().getVersions();
        if (affectedVersions.length == 0 || affectedVersions[0].getReleaseDate() == null || affectedVersions[0].getReleaseDate().compareTo(ov.getReleaseDate()) > 0) {    // not null and concistency check on IV
    
            // estimate injectedVersion using proportion
            iv = jiraDao.getAllReleased().get((int) estimateIvIndex(jiraDao.getReleaseOrderIndex(fv), jiraDao.getReleaseOrderIndex(ov)));
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
        return iv;
}
    
    @Override
    public void mine(MineDataBean bean) throws UnableToMineDataException {

        List<Release> allReleases;
        Release fv;
        Release iv;
        Release[] fixingVersions;
        RevCommit fixCommit;
        Set<String> changeSet;
        JiraDao jiraDao;
        GitDao gitDao;
        Release ov;
        List<JiraIssue> fixedBugs;
        
        // statistics on issues
        int withFvIssues = 0;
        int nonProductionIssues = 0;
        int noFixCommitIssues = 0;
        
        // initializes buggyness observations as not buggy
        bean.getObservationMatrix().getMatrix().forEach((releaseName, observations) -> 
            observations.forEach((resourceName, observation) -> 
                observation.put(Feature.BUGGYNESS, "no")
            ) 
        );
        
        // get all fixed bug issues in jira s.t issue.fixedVersion[0].date < lastRelease.date
        try {
            jiraDao = new JiraDao(this.jiraProject);
            gitDao = new GitDao(this.repo);

            allReleases = jiraDao.getAllReleased();
            fixedBugs = jiraDao.getTimeOrderedFixedBugIssues(new Date(0), allReleases.get(allReleases.size() - 1).getReleaseDate());

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
                                
                                iv = estimateIv(fixedBug, bean, jiraDao, ov, fv);                                
                                
                                // at this point we have a defined IV
                                putBuggyness(jiraDao, iv, fv, bean, changed);                                
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
