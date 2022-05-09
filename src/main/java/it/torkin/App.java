package it.torkin;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.lib.Repository;

import it.torkin.dao.git.GitDao;
import it.torkin.dao.git.UnableToGetReleasesException;
import it.torkin.entities.ObservationMatrix;
import it.torkin.entities.Release;
import it.torkin.miners.Feature;
import it.torkin.miners.UnknownFeatureException;

public class App 
{
    private static final Logger logger = Logger.getLogger(App.class.getName());

    // args

    /** Owner of the repository*/
    private static String repoOwner;
    
    /**name of repository*/
    private static String repoName;
    
    /** reference to the repository object */
    private static Repository repository = null;
    
    /** How many most recent release must be ignored to mitigate snoring */
    private static int lastReleasePercentageToIgnore;

    /** what features must be measured */
    private static Set<Feature> featuresToMeasure= EnumSet.noneOf(Feature.class);
    
    /**
     * All setup logic goes here
     * @throws UnableToAccessRepositoryException
     * @throws IOException
     * @throws UnableToGetReleasesException
     */
    private static void bootstrap() {
        /*
        // extracts list of releases and makes it global accessible
        GitDao gitDao = new GitDao(repoName);
            List<Release> releases = gitDao.getTimeOrederedReleases(new Date(0), new Date());

            // thrash away most recent releases
            int pivot = (lastReleasePercentageToIgnore / 100 * releases.size()) - 1;
            releases = releases.subList(0, pivot);
        */
  
    }
    
    /**
     * <p> parses command line arguments. </p>
     * <p> arg[0] : repoOwner/repoName string <p>
     * arg[1] : percentage of last releases to ignore <p>
     * arg[2] : comma-separated list of feature names to measure <p>
     * @param args args to parse
     * @throws InvalidArgumentsException
     */
    private static void parseArgs(String[] args) throws InvalidArgumentsException{
        if (args.length < 2){
            throw new InvalidArgumentsException("Not enough arguments.");
        }
        
        // args[0]
        String[] repoStringTokenized = args[0].split("/"); 
        repoOwner = repoStringTokenized[0];
        repoName = repoStringTokenized[1];

        // args[1]
        lastReleasePercentageToIgnore = Integer.parseInt(args[1]);
        
        // args[2]
        String[] requestedFeatureNames = args[2].split(",");
        for (String name : requestedFeatureNames){
            try {
                featuresToMeasure.add(Feature.getFeatureFromName(name));
            } catch (UnknownFeatureException e) {
                String msg = String.format("unrecognized feature name %s", e);
                logger.log(Level.WARNING, msg, e);
            }
        }        
    }

    public static void main( String[] args ) throws InvalidArgumentsException, UnableToAccessRepositoryException, UnableToGetReleasesException
    {
        parseArgs(args);

        GitDao gitDao = new GitDao(repoName);

        // gets list of releases to mine data from, discarding desired last releases percentage
        List<Release> releases = gitDao.getTimeOrederedReleases(new Date(0), new Date());
        int pivot = (lastReleasePercentageToIgnore / 100 * releases.size()) - 1;
        releases = releases.subList(0, pivot);

        // TODO: prepares observation matrix
        ObservationMatrix observationMatrix = new ObservationMatrix((Release[])releases.toArray());

        // TODO: call all implemented miners and make them mine data

        // TODO: dump observation matrix in a csv file. Each row is identified by a couple of releaseDate, resourceName

    }
}
