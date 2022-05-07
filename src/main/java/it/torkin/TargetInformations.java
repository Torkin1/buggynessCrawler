package it.torkin;

import java.util.ArrayList;
import java.util.List;

import it.torkin.entities.Release;

public class TargetInformations {

    private static TargetInformations ref = null;

    private final List<Release> releases;                 // List of available project releases, time ordered             
    
    private TargetInformations() {
        this.releases = new ArrayList<>();
    }

    public List<Release> getReleases() {
        return releases;
    }

    public static TargetInformations getReference(){
        if (ref == null){
            ref = new TargetInformations();
        }
        return ref;
    }    
}
