package it.torkin.dao.git;

import org.eclipse.jgit.api.CheckoutResult;

public class UnableToCheckoutReleaseException extends Exception{

    public UnableToCheckoutReleaseException(CheckoutResult.Status status) {
        super(String.format("checkout status is %s", status.toString()));
    }
    
    public UnableToCheckoutReleaseException(Exception e) {
        super(e);
    }

}
