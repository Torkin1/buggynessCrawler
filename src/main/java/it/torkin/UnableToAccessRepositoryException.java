package it.torkin;

public class UnableToAccessRepositoryException extends Exception{

    private final String repoName;
    
    public UnableToAccessRepositoryException(String repoName, Throwable cause){
        this.repoName = repoName;
    }

    public String getRepoName() {
        return repoName;
    }
}
