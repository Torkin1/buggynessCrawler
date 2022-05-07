package it.torkin;

import java.util.ArrayList;
import java.util.List;

import it.torkin.entities.Release;

public class Releases {

    private static Releases ref = null;

    private final List<Release> timeOrderedReleases;                 // List of available project releases, time ordered             
    
    private Releases() {
        this.timeOrderedReleases = new ArrayList<>();

        // TODO: move fetching and ordering of releases here
    }

    public List<Release> getReleases() {
        return timeOrderedReleases;
    }

    public static Releases getReference(){
        
        if (ref == null){
            ref = new Releases();
        }
        return ref;
    }    
}
