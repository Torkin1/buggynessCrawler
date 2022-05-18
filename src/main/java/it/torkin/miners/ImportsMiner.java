package it.torkin.miners;

import java.io.File;
import java.io.IOException;

import it.torkin.dao.git.GitDao;
import it.torkin.dao.git.UnableToAccessRepositoryException;

public class ImportsMiner extends Miner{

    public ImportsMiner(String owner, String project) {
        super(owner, project);
    }

    @Override
    public void mine(MineDataBean bean) throws UnableToMineDataException {
        
        try {
            GitDao gitDao = new GitDao(super.repo);
            File target = gitDao.getFile(bean.getResourceName());
            long imports = countKeywords(target, "import");
            bean
            .getObservationMatrix()
            .getMatrix()
            .get(bean.getTimeOrderedReleases().get(bean.getReleaseIndex()).getName())
            .get(bean.getResourceName())
            .put(Feature.IMPORTS, String.valueOf(imports));
        } catch (UnableToAccessRepositoryException | IOException e) {
            throw new UnableToMineImportsException(e);
        }

    }
    
}
