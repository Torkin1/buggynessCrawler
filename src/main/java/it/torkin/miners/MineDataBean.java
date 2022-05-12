package it.torkin.miners;


import java.util.List;

import it.torkin.dao.jira.JiraRelease;
import it.torkin.entities.ObservationMatrix;

public class MineDataBean {
    
    /**where measures will be collected
     */
    private ObservationMatrix observationMatrix;                    
    
    /**in which release we want to do the measures. Index is of time ordered releases list */
    private int releaseIndex;                                        
    
    /**which file has to be measured */
    private String resourceName;
    
    /**What releases must be considered, time ordered from oldest to most recent*/
    private List<JiraRelease> timeOrderedReleases;

    public ObservationMatrix getObservationMatrix() {
        return observationMatrix;
    }

    public int getReleaseIndex() {
        return releaseIndex;
    }

    public void setReleaseIndex(int releaseIndex) {
        this.releaseIndex = releaseIndex;
    }

    public void setObservationMatrix(ObservationMatrix observationMatrix) {
        this.observationMatrix = observationMatrix;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public List<JiraRelease> getTimeOrderedReleases() {
        return timeOrderedReleases;
    }

    public void setTimeOrderedReleases(List<JiraRelease> timeOrderedReleases) {
        this.timeOrderedReleases = timeOrderedReleases;
    }  
}
