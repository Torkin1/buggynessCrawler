package it.torkin.miners;

import java.io.File;
import java.io.IOException;

import it.torkin.dao.git.GitDao;
import it.torkin.dao.git.UnableToAccessRepositoryException;

public class PublicsMiner extends Miner {

    public PublicsMiner(String owner, String project) {
        super(owner, project);
    }
    
    @Override
    public void mine(MineDataBean bean) throws UnableToMineDataException {
        
        try {
            GitDao gitDao = new GitDao(super.repo);
            File target = gitDao.getFile(bean.getResourceName());
            long publics = countKeywords(target, "public");
            putObservation(bean, Feature.PUBLICS, publics);            
    
        } catch (UnableToAccessRepositoryException | IOException e) {
            throw new UnableToMinePublicsException(e);
        }
    }
    
}
