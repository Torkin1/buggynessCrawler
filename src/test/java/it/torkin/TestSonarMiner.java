package it.torkin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import it.torkin.dao.jira.JiraRelease;
import it.torkin.entities.ObservationMatrix;
import it.torkin.miners.MineDataBean;
import it.torkin.miners.Miner;
import it.torkin.miners.UnableToMineDataException;
import it.torkin.miners.CodeSmellsMiner;
import it.torkin.miners.Feature;

class TestSonarMiner {
    
    @Test
    void testMineSmells() throws ParseException, UnableToMineDataException{

        JiraRelease release = new JiraRelease();
        JiraRelease[] releases = new JiraRelease[1];
        releases[0] = release;
        release.setName("testRelease");
        release.setReleaseDate((new SimpleDateFormat("yyyy-mm-dd")).parse("2019-06-25"));

        ObservationMatrix observationMatrix = new ObservationMatrix(releases);

        observationMatrix.getMatrix().get(release.getName()).put("lang/java/avro/src/main/java/org/apache/avro/message/BinaryMessageDecoder.java", new HashMap<>());

        MineDataBean bean = new MineDataBean();
        bean.setObservationMatrix(observationMatrix);
        //bean.setRelease(release);
        bean.setResourceName("lang/java/avro/src/main/java/org/apache/avro/message/BinaryMessageDecoder.java");
        
        Miner miner = new CodeSmellsMiner("torkin1", "avro");
        miner.mine(bean);

        assertEquals("1",
                     bean
                        .getObservationMatrix()
                        .getMatrix()
                        .get(release.getName())
                        .get("lang/java/avro/src/main/java/org/apache/avro/message/BinaryMessageDecoder.java")
                        .get(Feature.CODE_SMELLS)
                        );
    }
}
