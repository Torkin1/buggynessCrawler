package it.torkin.miners;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class BuggynessMiner extends Miner {

    private final String jiraProject;

    /** last release labeled from this miner */
    private JiraRelease lastRelease;

    /** proportion */
    private double p;

    public BuggynessMiner(String owner, String project) {
        super(owner, project);
        this.jiraProject = project.toUpperCase();
        this.lastRelease = new JiraRelease();
        this.lastRelease.setReleaseDate(new Date(0));
        this.lastRelease.setName("");
        this.p = 0;
    }

    public String getJiraProject() {
        return jiraProject;
    }

    @Override
    public void mine(MineDataBean bean) throws UnableToMineDataException {

        // get all fixed bug issues in jira s.t lastRelease.date < issue.fixedVersion[0].date < release.date
        try {
            JiraDao jiraDao = new JiraDao(this.jiraProject);
            GitDao gitDao = new GitDao(this.repo);
            JiraRelease ov = bean.getTimeOrderedReleases().get(bean.getReleaseIndex());
            List<JiraIssue> fixedBugs = jiraDao.getAllFixedBugIssues(new Date(0), ov.getReleaseDate());
            JiraRelease fv;
            JiraRelease iv;
            Date ovDate;
            JiraRelease[] fixingVersions;
            JiraRelease[] affectedVersions;
            RevCommit fixCommit;
            Set<String> changeSet;

            for (JiraIssue fixedBug : fixedBugs) {

                ovDate = fixedBug.getFields().getCreated();

                // get latest commit older than fixedVersion s.t. commit.date < issue.fixedVersion.date and commit.comment.contains(issue.key)
                fixingVersions = fixedBug.getFields().getFixVersions();
                if (fixingVersions.length > 0) {
                    fv = fixingVersions[fixingVersions.length - 1]; // will use only most recent fixing version
                    fixCommit = gitDao.getLatestCommit(fv.getReleaseDate(), fixedBug.getKey()); // assuming that the fix commit is the most recent one before fv release date which includes JIRA bug id in it's description

                    if (fixCommit != null) { // some silly dev could have forgot to include jira issue key in fixing commit
                        changeSet = gitDao.getCommitChangeSet(fixCommit);
                        for (String changed : changeSet) {
                            affectedVersions = fixedBug.getFields().getVersions();
                            if (affectedVersions.length != 0) { // TODO: remove this enclosing if when P calculation is implemented
                                if (affectedVersions.length < 0) {

                                    // TODO: calculate injectedVersion using proportion

                                    // TODO: issue.affectedVersions.add(injectedVersion)
                                }

                                // TODO: update P value

                                iv = affectedVersions[0]; // IV is the earliest among affected releases

                                if (iv.getReleaseDate().compareTo(ovDate) <= 0) { // consistency check: IV must be prior to OV, else we discard the issue
                                    for (JiraRelease av : jiraDao.getAllReleased(iv.getReleaseDate(), fv.getReleaseDate())) {   // better not rely entirely on AVs listed in JIRA, as they are often wrong.
                                        Map<Feature, String> measure = bean.getObservationMatrix().getMatrix().get(av.getName()).get(changed);
                                        if (measure != null) { // changed file may be absent in current av
                                            measure.put(Feature.BUGGYNESS, "yes");
                                        }
                                    }
                                }
                            }
                        }

                    }
                }

            }
        } catch (UnableToGetAllFixedBugsException | UnableToAccessRepositoryException | UnableToGetCommitsException
                | UnableToGetChangeSetException | UnableToGetReleasesException e) {
            throw new UnableToMineBuggynessException(e);
        }

    }

}
