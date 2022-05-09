package it.torkin.dao.git;

public class UnableToGetReleasesException extends Exception {
    
    public UnableToGetReleasesException(Exception e) {
        super(e);
    }

}
