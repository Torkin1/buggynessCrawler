package it.torkin.miners;

public class UnknownFeatureException extends Exception{
                
    public UnknownFeatureException(String name){
        
        super(String.format("No known feature with name %s found", name));
    }
}
