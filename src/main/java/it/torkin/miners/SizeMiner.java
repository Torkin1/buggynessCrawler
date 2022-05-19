package it.torkin.miners;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import it.torkin.dao.git.GitDao;
import it.torkin.dao.git.UnableToAccessRepositoryException;
import it.torkin.entities.Release;

public class SizeMiner extends Miner{

    public SizeMiner(String owner, String project) {
        super(owner, project);
    }

    private long countLOCs(File file) throws IOException{
        try (LineNumberReader reader = new LineNumberReader(new FileReader(file))) {
            return reader.lines().count();
        }
    }
    
    @Override
    public void mine(MineDataBean bean) throws UnableToMineDataException {
        
        /*
        try {
            Release targetRelease = bean.getTimeOrderedReleases().get(bean.getReleaseIndex());
            GitDao gitDao = new GitDao(super.repo);
            File targetResource = gitDao.getFile(bean.getResourceName());
            bean
                .getObservationMatrix()
                .getMatrix()
                .get(targetRelease.getName())
                .get(bean.getResourceName())
                .put(Feature.SIZE, String.valueOf(countLOCs(targetResource)));

        } catch (UnableToAccessRepositoryException | IOException e) {
            
            throw new UnableToMineSizeException(e);
        }
    */
    }
    
    
}
