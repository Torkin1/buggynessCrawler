package it.torkin.miners;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import it.torkin.dao.git.GitDao;
import it.torkin.dao.git.UnableToAccessRepositoryException;

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
        
        try {
            GitDao gitDao = new GitDao(super.repo);
            File targetResource = gitDao.getFile(bean.getResourceName());
            long locs = countLOCs(targetResource);
            putObservation(bean, Feature.SIZE, locs);
        } catch (UnableToAccessRepositoryException | IOException e) {
            
            throw new UnableToMineSizeException(e);
        }
    }
    
    
}
