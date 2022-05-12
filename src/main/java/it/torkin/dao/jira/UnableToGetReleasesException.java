package it.torkin.dao.jira;

public class UnableToGetReleasesException extends Exception {
    
    public UnableToGetReleasesException(Exception e) {
        super(e);
    }

}
