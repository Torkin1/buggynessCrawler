package it.torkin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.torkin.dao.git.GitDao;
import it.torkin.dao.git.UnableToAccessRepositoryException;
import it.torkin.dao.git.UnableToGetCommitsException;
import it.torkin.dao.git.UnableToGetFileNamesException;
import it.torkin.dao.jira.JiraRelease;

class TestGitDao {
    
    @Test
    void testGetFileNamesList() throws UnableToAccessRepositoryException, UnableToGetFileNamesException, UnableToGetCommitsException{

        GitDao dao = new GitDao("avro");
        String expected = "doc/examples/java-example/src/main/java/example/GenericMain.java";   //git ls-tree release-1.10.2 --name-only -r | grep ".*\.java$" | head -n 1

        JiraRelease release = new JiraRelease();
        release.setName("release-1.10.2");
        // no need to set release date
        
        List<String> fileNames = dao.getFileNames(dao.getLatestCommit(release.getReleaseDate()));

        String actual = fileNames.get(0);
        assertEquals(expected, actual);


    }
}
