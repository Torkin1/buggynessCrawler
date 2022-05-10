package it.torkin;

import it.torkin.miners.Feature;

public class UnableToPrepareMinersException extends Exception{

    public UnableToPrepareMinersException(Class<? extends Feature> class1, String name, Exception cause) {
        super(String.format("Unable to prepare miner implementation %s for feature %s", class1.getName(), name, cause));
    }

}
