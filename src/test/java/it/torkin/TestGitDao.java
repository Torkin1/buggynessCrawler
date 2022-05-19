package it.torkin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.torkin.dao.git.GitDao;
import it.torkin.dao.git.UnableToAccessRepositoryException;
import it.torkin.dao.git.UnableToCheckoutReleaseException;
import it.torkin.dao.git.UnableToGetCommitsException;
import it.torkin.dao.git.UnableToGetFileNamesException;
import it.torkin.dao.jira.JiraDao;
import it.torkin.dao.jira.UnableToGetReleasesException;
import it.torkin.entities.Release;

class TestGitDao {
    
    @Test
    void testGetFileNamesList() throws UnableToAccessRepositoryException, UnableToGetFileNamesException, UnableToGetCommitsException, ParseException{

        GitDao dao = new GitDao("avro");
        String expected = "doc/examples/java-example/src/main/java/example/GenericMain.java";   //git ls-tree release-1.10.2 --name-only -r | grep ".*\.java$" | head -n 1

        Release release = new Release();
        release.setName("1.10.2");
        release.setReleaseDate(new SimpleDateFormat("yyyy-MM-dd").parse("2021-03-15"));
        
        List<String> fileNames = dao.getFileNames(dao.getLatestCommit(release.getReleaseDate()));

        String actual = fileNames.get(0);
        assertEquals(expected, actual);


    }

    @Test
    void testCheckout() throws UnableToAccessRepositoryException, UnableToGetReleasesException, UnableToCheckoutReleaseException{
        GitDao gitDao = new GitDao("avro");
        JiraDao jiraDao = new JiraDao("AVRO");

        List<Release> releases = jiraDao.getAllReleased();
        gitDao.checkout(releases.get(0));
    }
}
