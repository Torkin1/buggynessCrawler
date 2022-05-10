package it.torkin;

public class UnableToPrepareObservationMatrixException extends Exception {

    public UnableToPrepareObservationMatrixException(UnableToAccessRepositoryException e) {
        super(e);
    }

}
