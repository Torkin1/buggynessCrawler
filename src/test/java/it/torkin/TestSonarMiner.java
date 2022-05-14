package it.torkin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.torkin.dao.jira.JiraRelease;
import it.torkin.dao.jira.UnableToGetReleasesException;
import it.torkin.entities.ObservationMatrix;
import it.torkin.miners.MineDataBean;
import it.torkin.miners.Miner;
import it.torkin.miners.UnableToMineDataException;
import it.torkin.miners.CodeSmellsMiner;
import it.torkin.miners.Feature;

class TestSonarMiner {
    
    @Test
    void testMineSmells() throws ParseException, UnableToMineDataException, UnableToGetReleasesException{

        List<JiraRelease> releases = new ArrayList<>();
        JiraRelease release = new JiraRelease();
        releases.add(release);
        release.setName("testRelease");
        release.setReleaseDate((new SimpleDateFormat("yyyy-MM-dd")).parse("2019-06-26"));

        ObservationMatrix observationMatrix = new ObservationMatrix(releases.toArray(new JiraRelease[0]));

        observationMatrix.getMatrix().get(release.getName()).put("lang/java/avro/src/main/java/org/apache/avro/message/BinaryMessageDecoder.java", new HashMap<>());

        MineDataBean bean = new MineDataBean();
        bean.setObservationMatrix(observationMatrix);
        bean.setTimeOrderedReleases(releases);
        bean.setResourceName("lang/java/avro/src/main/java/org/apache/avro/message/BinaryMessageDecoder.java");
        bean.setReleaseIndex(0);
        
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
