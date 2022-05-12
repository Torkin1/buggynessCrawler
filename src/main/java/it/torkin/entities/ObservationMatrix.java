package it.torkin.entities;

import java.util.HashMap;
import java.util.Map;

import it.torkin.dao.jira.JiraRelease;
import it.torkin.miners.Feature;

public class ObservationMatrix {
    
    /**
     * A matrix holding observations. Every cell identifies a resource in a particular release.
     */
    private final Map<String, Map<String, Map<Feature, String>>> matrix;

    public ObservationMatrix(JiraRelease[] releases){

        this.matrix = new HashMap<>();
        for (JiraRelease r : releases){
            matrix.put(r.getName(), new HashMap<>());
        }
    }

    public Map<String, Map<String, Map<Feature, String>>> getMatrix() {
        return matrix;
    }
}
