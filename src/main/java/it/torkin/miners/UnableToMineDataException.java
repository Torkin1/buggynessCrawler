package it.torkin.miners;

public class UnableToMineDataException extends Exception {

    public UnableToMineDataException(Exception e){
        super(e);
    }
}
