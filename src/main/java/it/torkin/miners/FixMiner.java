package it.torkin.miners;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.revwalk.RevCommit;

import it.torkin.dao.git.GitDao;
import it.torkin.dao.git.UnableToAccessRepositoryException;
import it.torkin.dao.git.UnableToGetChangeSetException;
import it.torkin.dao.git.UnableToGetCommitsException;
import it.torkin.dao.jira.JiraDao;
import it.torkin.dao.jira.JiraIssue;
import it.torkin.dao.jira.UnableToGetAllFixedBugsException;
import it.torkin.entities.Release;

public class FixMiner extends Miner{

    public FixMiner(String owner, String project) {
        super(owner, project);
        
    }

    @Override
    public void mine(MineDataBean bean) throws UnableToMineDataException {
        
        Release targetRelease = bean.getTimeOrderedReleases().get(bean.getReleaseIndex());
        JiraDao jiraDao = new JiraDao(super.repo.toUpperCase());
        long nFix = 0;
        try {
            GitDao gitDao = new GitDao(super.repo);
            List<JiraIssue> fixedBugs = jiraDao.getTimeOrderedFixedBugIssues(new Date(0), targetRelease.getReleaseDate());
            for (JiraIssue fixedBug : fixedBugs){
                
                // get fix commit, namely most recent commit before fv date with issue key in commit description
                Release[] fixingVersions = fixedBug.getFields().getFixVersions();
                Release fv = fixingVersions[fixingVersions.length - 1]; // will use only most recent fixing version
                RevCommit fixCommit = gitDao.getLatestCommit(fv.getReleaseDate(), fixedBug.getKey());

                if (fixCommit != null){
                    // count each time target resource is in a fix commit changeset
                    Set<String> changeset = gitDao.getCommitChangeSet(fixCommit);
                    if (changeset.contains(bean.getResourceName())){
                        nFix ++;
                    }
                }
            }
            putObservation(bean, Feature.N_FIX, nFix);
        } catch (UnableToGetAllFixedBugsException | UnableToGetCommitsException | UnableToGetChangeSetException | UnableToAccessRepositoryException e) {
            throw new UnableToMineFixesException(e);
        }
    }

}
