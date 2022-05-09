package it.torkin.dao.git;

import java.io.IOException;

public class UnableToGetFileNamesException extends Exception{

    public UnableToGetFileNamesException(String release, IOException e) {
        super(String.format("Unable to get files of release %s", release), e);
    }

}
