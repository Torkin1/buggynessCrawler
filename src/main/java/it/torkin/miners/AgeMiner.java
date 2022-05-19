package it.torkin.miners;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import org.eclipse.jgit.revwalk.RevCommit;

import it.torkin.dao.git.GitDao;
import it.torkin.dao.git.UnableToAccessRepositoryException;
import it.torkin.dao.git.UnableToGetCommitsException;
import it.torkin.entities.Release;

public class AgeMiner extends Miner {

    public AgeMiner(String owner, String project) {
        super(owner, project);
    }

    @Override
    public void mine(MineDataBean bean) throws UnableToMineDataException {
        
            GitDao gitDao;
            try {
                gitDao = new GitDao(super.repo);
                Release targetRelease = bean.getTimeOrderedReleases().get(bean.getReleaseIndex());

                // calculates age from oldest commit to today
                RevCommit oldest = gitDao.getOldestCommit(bean.getResourceName());
                LocalDateTime oldestDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(oldest.getAuthorIdent().getWhen().getTime()), ZoneId.of(oldest.getAuthorIdent().getTimeZone().getID()));
                LocalDateTime releaseDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(targetRelease.getReleaseDate().getTime()), ZoneId.systemDefault());
                long weeks = ChronoUnit.WEEKS.between(oldestDate, releaseDate);
    
                putObservation(bean, Feature.AGE, weeks);
    
            } catch (UnableToAccessRepositoryException | UnableToGetCommitsException e) {
                throw new UnableToMineAgeException(e);
            }   
        }
    
}
