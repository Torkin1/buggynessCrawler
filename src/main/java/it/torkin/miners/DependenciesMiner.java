package it.torkin.miners;

import java.io.File;
import java.io.IOException;

import it.torkin.dao.git.GitDao;
import it.torkin.dao.git.UnableToAccessRepositoryException;

public class DependenciesMiner extends Miner{

    public DependenciesMiner(String owner, String project) {
        super(owner, project);
    }

    @Override
    public void mine(MineDataBean bean) throws UnableToMineDataException {
        
        try {
            GitDao gitDao = new GitDao(super.repo);
            File target = gitDao.getFile(bean.getResourceName());
            long dependencies = countKeywords(target, "import", "extends", "implements");
            putObservation(bean, Feature.DEPENDENCIES, dependencies);
        } catch (UnableToAccessRepositoryException | IOException e) {
            throw new UnableToMineImportsException(e);
        }

    }
    
}
