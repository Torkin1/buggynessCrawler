package it.torkin.dao.git;

public class UnableToGetCommitsException extends Exception{

    public UnableToGetCommitsException(Exception e){
        super(e);
    }

}
