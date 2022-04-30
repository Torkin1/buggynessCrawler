package it.torkin;

public class UnableToAccessRepositoryException extends Exception{

    public UnableToAccessRepositoryException(String msg, Throwable cause){
        super(msg, cause);
    }
}
