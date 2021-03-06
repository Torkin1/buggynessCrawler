package it.torkin.miners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public abstract class Miner {
    
    /**
     * mines features specified in bean for a given resource in a given release.
     * @param bean
     * @throws UnableToMineDataException
     */
    public abstract void mine(MineDataBean bean) throws UnableToMineDataException;
    
    protected String owner;
    protected String repo;
    
    protected long countKeywords(File target, String... keywords) throws IOException {
        long matches = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(target));) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String[] statements = line.split(";");
                for (String s : statements) {
                    for (String k : keywords) {
                        if (s.contains(k + " ")) {
                            matches++;
                        }
                    }
                }
            }
        }
        return matches;
    }

    protected Miner(String owner, String project){
        this. owner = owner;
        this.repo = project;
    }

    protected void putObservation(MineDataBean bean, Feature feature, Object measure){
        bean.getObservationMatrix()
                .getMatrix()
                .get(bean.getTimeOrderedReleases().get(bean.getReleaseIndex()).getName())
                .get(bean.getResourceName())
                .put(feature, String.valueOf(measure));
    }
}
