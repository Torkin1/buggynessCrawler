package it.torkin.rest;

import java.io.IOException;

import org.restlet.ext.gson.GsonRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

public class ClientResourceGetter<T> {

    private final Class<T> resourceType;

    public ClientResourceGetter(Class<T> resourceType){
        this.resourceType = resourceType;
    }
    
    public T getClientResourceObject(String query) throws UnableToGetResourceObjectException {
        try {
            GsonRepresentation<T> gsonRepresentation = new ClientResource(query).get(GsonRepresentation.class); 
            gsonRepresentation.setObjectClass(resourceType);
            return gsonRepresentation.getObject();
        } catch (ResourceException | IOException e) {
            
            throw new UnableToGetResourceObjectException(e);
        }

    }
    
}
