package it.torkin.miners;


import it.torkin.entities.ObservationMatrix;
import it.torkin.entities.Release;

public class MineDataBean {
    
    /**where measures will be collected
     */
    private ObservationMatrix observationMatrix;                    
    
    /**in which release we want to do the measures */
    private Release release;                                        
    
    /**which file has to be measured */
    private String resourceName;                                    

    public ObservationMatrix getObservationMatrix() {
        return observationMatrix;
    }

    public void setObservationMatrix(ObservationMatrix observationMatrix) {
        this.observationMatrix = observationMatrix;
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
