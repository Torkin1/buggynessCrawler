package it.torkin.miners;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import it.torkin.dao.git.GitDao;
import it.torkin.dao.git.UnableToAccessRepositoryException;
import it.torkin.dao.git.UnableToGetCommitsException;

public class AuthorsMiner extends Miner{

    public AuthorsMiner(String owner, String project) {
        super(owner, project);
    }

    @Override
    public void mine(MineDataBean bean) throws UnableToMineDataException {

        // we seek for different authors in list of commits related to target resource
        Set<String> seen = new HashSet<>();
        try {
            GitDao gitDao = new GitDao(super.repo);
            List<RevCommit> commits = gitDao.getAllCommits(bean.getResourceName());
            int authors = 0;
            PersonIdent author;
            for (RevCommit commit : commits){
                author = commit.getAuthorIdent();
                if (author != null){
                    seen.add(author.getName());
                }
            }
            authors = seen.size();
            putObservation(bean, Feature.N_AUTH, authors);

            
        } catch (UnableToAccessRepositoryException | UnableToGetCommitsException e) {
            
            throw new UnableToMineAuthorsException(e);
        }
        
    }
    
}
