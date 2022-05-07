package it.torkin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.EnumSet;

import org.junit.jupiter.api.Test;

import it.torkin.entities.Observation;
import it.torkin.entities.Release;
import it.torkin.miners.Feature;
import it.torkin.miners.MineDataBean;
import it.torkin.miners.Miner;
import it.torkin.miners.SonarMiner;

public class testSonarMiner {
    
    @Test
    void testMineSmells() throws ParseException{

        Observation observation = new Observation();
        Release release = new Release();

        release.setName("testRelease");
        release.setReleaseDate((new SimpleDateFormat("yyyy-mm-dd")).parse("2019-06-25"));
        observation.setResourceName("lang/java/avro/src/main/java/org/apache/avro/message/BinaryMessageDecoder.java");
        observation.setRelease(release);

        MineDataBean bean = new MineDataBean();
        bean.setMetrics(EnumSet.of(Feature.CODE_SMELLS));
        bean.setObservation(observation);
        
        Miner miner = new SonarMiner("torkin1", "Torkin1_avro");
        miner.mineData(bean);

        assertEquals(1, observation.getnSmells());
        
    }
}
