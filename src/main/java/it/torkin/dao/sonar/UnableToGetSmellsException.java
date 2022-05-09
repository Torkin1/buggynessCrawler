package it.torkin.dao.sonar;

import it.torkin.rest.UnableToGetResourceObjectException;

public class UnableToGetSmellsException extends Exception {

    public UnableToGetSmellsException(UnableToGetResourceObjectException e) {
        super(e);
    }



}
