package it.torkin.miners;

import java.util.Set;

import it.torkin.entities.Observation;

public class MineDataBean {

    private Set<Feature> features;                                  // which features of observed resource must be measured 
    private Observation observation;                                // where measures will be collected

    public Observation getObservation() {
        return observation;
    }

    public Set<Feature> getMetrics() {
        return features;
    }

    public void setMetrics(Set<Feature> metrics) {
        this.features = metrics;
    }

    public void setObservation(Observation observation) {
        this.observation = observation;
    }

}
