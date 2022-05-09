package it.torkin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.jupiter.api.Test;

import it.torkin.entities.Observation;
import it.torkin.entities.ObservationMatrix;
import it.torkin.entities.Release;
import it.torkin.miners.MineDataBean;
import it.torkin.miners.Miner;
import it.torkin.miners.CodeSmellsMiner;

class TestSonarMiner {
    
    @Test
    void testMineSmells() throws ParseException{

        Observation observation = new Observation();
        Release release = new Release();
        Release[] releases = new Release[1];
        releases[0] = release;
        release.setName("testRelease");
        release.setReleaseDate((new SimpleDateFormat("yyyy-mm-dd")).parse("2019-06-25"));

        ObservationMatrix observationMatrix = new ObservationMatrix(releases);

        observation.setResourceName("lang/java/avro/src/main/java/org/apache/avro/message/BinaryMessageDecoder.java");
        observation.setRelease(release);
        observationMatrix.getMatrix().get(release.getName()).put(observation.getResourceName(), observation);

        MineDataBean bean = new MineDataBean();
        bean.setObservationMatrix(observationMatrix);
        bean.setRelease(release);
        bean.setResourceName(observation.getResourceName());
        
        Miner miner = new CodeSmellsMiner("torkin1", "Torkin1_avro");
        miner.mine(bean);

        assertEquals(1,
                     bean
                        .getObservationMatrix()
                        .getMatrix()
                        .get(release.getName())
                        .get(observation.getResourceName())
                        .getnSmells()
                        );
    }
}
