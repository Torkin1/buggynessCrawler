package it.torkin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.torkin.dao.git.GitDao;
import it.torkin.dao.git.UnableToGetFileNamesException;
import it.torkin.entities.Release;

public class TestGitDao {
    
    @Test
    void testGetFileNamesList() throws UnableToAccessRepositoryException, UnableToGetFileNamesException{

        GitDao dao = new GitDao("avro");
        String expected = "doc/examples/java-example/src/main/java/example/GenericMain.java";   //git ls-tree release-1.10.2 --name-only -r | grep ".*\.java$" | head -n 1

        Release release = new Release();
        release.setName("release-1.10.2");
        // no need to set release date
        
        List<String> fileNames = dao.getFileNamesOfRelease(release.getName());

        String actual = fileNames.get(0);
        assertEquals(expected, actual);


    }
}
