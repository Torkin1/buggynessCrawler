package it.torkin.dao.jira;

import it.torkin.rest.UnableToGetResourceObjectException;

public class UnableToGetAllFixedBugsException extends Exception{

    public UnableToGetAllFixedBugsException(UnableToGetResourceObjectException e) {
        super(e);
    }

}
