package it.torkin.miners;

import java.util.Set;

import it.torkin.entities.ObservationMatrix;
import it.torkin.entities.Release;

public class MineDataBean {

    /** which features of observed resource must be measured */
    private Set<Feature> features;                                  
    
    /**where measures will be collected
     */
    private ObservationMatrix observationMatrix;                    
    
    /**in which release we want to do the measures */
    private Release release;                                        
    
    /**which file has to be measured */
    private String resourceName;                                    

    public Set<Feature> getFeatures() {
        return features;
    }

    public void setMetrics(Set<Feature> metrics) {
        this.features = metrics;
    }

    public ObservationMatrix getObservationMatrix() {
        return observationMatrix;
    }

    public void setObservationMatrix(ObservationMatrix observationMatrix) {
        this.observationMatrix = observationMatrix;
    }

    public void setFeatures(Set<Feature> features) {
        this.features = features;
    }

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release = release;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    
}
