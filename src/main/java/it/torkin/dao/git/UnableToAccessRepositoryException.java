package it.torkin.dao.git;

public class UnableToAccessRepositoryException extends Exception{

    private final String repoName;
    
    public UnableToAccessRepositoryException(String repoName, Throwable cause){
        super(cause);
        this.repoName = repoName;
    }

    public String getRepoName() {
        return repoName;
    }
}
