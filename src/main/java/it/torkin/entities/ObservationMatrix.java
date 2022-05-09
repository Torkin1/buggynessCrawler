package it.torkin.entities;

import java.util.HashMap;
import java.util.Map;

public class ObservationMatrix {
    
    /**
     * A matrix holding observations. Every cell identifies a resource in a particular release.
     */
    private final Map<String, Map<String, Observation>> matrix;

    public ObservationMatrix(Release[] releases){

        this.matrix = new HashMap<>();
        for (Release r : releases){
            matrix.put(r.getName(), new HashMap<>());
        }
    }

    public Map<String, Map<String, Observation>> getMatrix() {
        return matrix;
    }
}
