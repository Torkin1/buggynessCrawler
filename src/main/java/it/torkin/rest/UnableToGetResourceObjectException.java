package it.torkin.rest;

public class UnableToGetResourceObjectException extends Exception {
    public UnableToGetResourceObjectException(Exception cause){
        super(cause);
    }
}
