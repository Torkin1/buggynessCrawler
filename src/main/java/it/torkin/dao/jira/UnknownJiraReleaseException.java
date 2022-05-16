package it.torkin.dao.jira;

public class UnknownJiraReleaseException extends Exception{

    public UnknownJiraReleaseException(String name) {
        super(String.format("release %s isn't among released", name));
    }

}
