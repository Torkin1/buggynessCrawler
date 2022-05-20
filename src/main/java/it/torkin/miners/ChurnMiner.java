package it.torkin.miners;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.Edit.Type;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import it.torkin.dao.git.GitDao;
import it.torkin.dao.git.UnableToAccessRepositoryException;
import it.torkin.dao.git.UnableToDoDiffException;
import it.torkin.dao.git.UnableToGetCommitsException;

public class ChurnMiner extends Miner{

    public ChurnMiner(String owner, String project) {
        super(owner, project);
    }

    private long calculateChurn(GitDao gitDao, RevCommit commit, String fileName) throws UnableToMineTouchedLOCsException{
                
        try (DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE)) {

            List<Edit> currentDiffEdits;
            long added = 0;
            long deleted = 0;
            RevCommit parent;
            
            // gets diffs of given file related to given commit
            parent = (commit.getParents().length != 0)? commit.getParent(0) : null;
            List<DiffEntry> diffs = gitDao.getDiffs(parent, commit);
            diffs.removeIf(diff -> diff.getNewPath().compareTo(fileName) != 0);

            df.setRepository(gitDao.getRepository());
            for (DiffEntry diff : diffs) {
                currentDiffEdits = df.toFileHeader(diff).toEditList();
                currentDiffEdits.removeIf(edit -> edit.getType() == Type.EMPTY);
                for (Edit edit : currentDiffEdits) {
                    deleted += edit.getEndA() - edit.getBeginA();
                    added += edit.getEndB() - edit.getBeginB();
                }
            }

            return added - deleted;

        } catch (UnableToDoDiffException | IOException e) {
            throw new UnableToMineTouchedLOCsException(e);
        }
    }
    
    @Override
    public void mine(MineDataBean bean) throws UnableToMineDataException {
        try {
            long churn = 0;
            
            GitDao gitDao = new GitDao(super.repo);
            Date startDate;
            Date endDate;
            
            // sums churn over each commit related to target release
            endDate = bean.getTimeOrderedReleases().get(bean.getReleaseIndex()).getReleaseDate();
            startDate = (bean.getReleaseIndex() == 0)? new Date(0) : bean.getTimeOrderedReleases().get(bean.getReleaseIndex() - 1).getReleaseDate();
            List<RevCommit> commits = gitDao.getAllCommits(bean.getResourceName(), startDate, endDate);
            for (RevCommit commit : commits){
                
                churn += calculateChurn(gitDao, commit, bean.getResourceName());
            }
            putObservation(bean, Feature.CHURN, churn);
 
        } catch (UnableToAccessRepositoryException | UnableToGetCommitsException e) {
            
            throw new UnableToMineTouchedLOCsException(e);
        }
    }

}
