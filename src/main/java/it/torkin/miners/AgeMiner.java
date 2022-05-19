package it.torkin.miners;

import it.torkin.dao.git.GitDao;
import it.torkin.dao.git.UnableToAccessRepositoryException;
import it.torkin.dao.git.UnableToGetCommitsException;
import it.torkin.entities.Release;

public class AgeMiner extends Miner{

    public AgeMiner(String owner, String project) {
        super(owner, project);
    }

    @Override
    public void mine(MineDataBean bean) throws UnableToMineDataException {
        
        try {
            GitDao gitDao = new GitDao(super.repo);
            Release targetRelease = bean.getTimeOrderedReleases().get(bean.getReleaseIndex());
            long age = gitDao.getAllCommits(bean.getResourceName(), targetRelease.getReleaseDate()).size();
            registerObservation(bean, age);
        } catch (UnableToAccessRepositoryException | UnableToGetCommitsException e) {
            
            throw new UnableToMineAgeException(e);
        }
        
    }
    
}
