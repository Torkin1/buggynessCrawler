package it.torkin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.EnumSet;

import org.junit.jupiter.api.Test;

import it.torkin.entities.Observation;
import it.torkin.entities.ObservationMatrix;
import it.torkin.entities.Release;
import it.torkin.miners.Feature;
import it.torkin.miners.MineDataBean;
import it.torkin.miners.Miner;
import it.torkin.miners.SonarMiner;

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
        bean.setMetrics(EnumSet.of(Feature.CODE_SMELLS));
        bean.setObservationMatrix(observationMatrix);
        bean.setRelease(release);
        bean.setResourceName(observation.getResourceName());
        
        Miner miner = new SonarMiner("torkin1", "Torkin1_avro");
        miner.mineData(bean);

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
