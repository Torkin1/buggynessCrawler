package it.torkin.miners;

public abstract class UnableToMineDataException extends Exception {

    protected UnableToMineDataException(Exception e){
        super(e);
    }
}
