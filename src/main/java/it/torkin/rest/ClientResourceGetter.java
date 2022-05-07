package it.torkin.rest;

import java.io.IOException;

import org.restlet.ext.gson.GsonRepresentation;
import org.restlet.resource.ClientResource;

public class ClientResourceGetter<T> {

    private final Class<T> resourceType;

    public ClientResourceGetter(Class<T> resourceType){
        this.resourceType = resourceType;
    }
    
    public T getClientResourceObject(String query) throws IOException{
        GsonRepresentation<T> gsonRepresentation = new ClientResource(query).get(GsonRepresentation.class); 
        gsonRepresentation.setObjectClass(resourceType);
        return gsonRepresentation.getObject();

    }
    
}
