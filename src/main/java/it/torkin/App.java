package it.torkin;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import it.torkin.dao.git.GitDao;
import it.torkin.dao.git.UnableToGetFileNamesException;
import it.torkin.dao.git.UnableToGetReleasesException;
import it.torkin.entities.ObservationMatrix;
import it.torkin.entities.Release;
import it.torkin.miners.Feature;
import it.torkin.miners.MineDataBean;
import it.torkin.miners.Miner;
import it.torkin.miners.UnknownFeatureException;

public class App 
{
    private static final Logger logger = Logger.getLogger(App.class.getName());

    // args

    /** Owner of the repository*/
    private static String repoOwner;
    
    /**name of repository*/
    private static String repoName;
        
    /** How many most recent release must be ignored to mitigate snoring */
    private static int lastReleasePercentageToIgnore;

    /** what features must be measured */
    private static Set<Feature> featuresToMeasure= EnumSet.noneOf(Feature.class);
    
    
    /**
     * <p> parses command line arguments. </p>
     * <p> arg[0] : repoOwner/repoName string <p>
     * arg[1] : percentage of last releases to ignore <p>
     * arg[2] : comma-separated list of feature names to measure <p>
     * @param args args to parse
     * @throws InvalidArgumentsException
     */
    private static void parseArgs(String[] args) throws InvalidArgumentsException{
        if (args.length < 3){
            throw new InvalidArgumentsException("Not enough arguments.");
        }
        
        // args[0]
        String[] repoStringTokenized = args[0].split("/"); 
        repoOwner = repoStringTokenized[0];
        repoName = repoStringTokenized[1];

        // args[1]
        lastReleasePercentageToIgnore = Math.min(Integer.parseInt(args[1]), 100);
        
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
        // featuresToMeasure.add(Feature.BUGGYNESS);
    }

    public static void main( String[] args ) throws InvalidArgumentsException, UnableToAccessRepositoryException, UnableToGetReleasesException
    {
        parseArgs(args);

        List<Miner> miners;
        List<Release> releases;
        ObservationMatrix observationMatrix;
        MineDataBean mineDataBean;

        // Creates miner for desired measures. If a feature miner cannot be found, the feature is removed from resuested features
        miners = new ArrayList<>();
        for (Feature f : featuresToMeasure) {
            
            try {
                miners.add(f.getMiner().getConstructor(String.class, String.class).newInstance(repoOwner, repoName));
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                String msg = String.format("Unable to find implementation %s to mine feature %s", f.getClass().getSimpleName(), f.getName());
                        logger.log(Level.WARNING, msg, e);
                        featuresToMeasure.remove(f);
            }
        }        

        GitDao gitDao = new GitDao(repoName);

        // gets list of releases to mine data from, discarding desired last releases percentage
        releases = gitDao.getTimeOrederedReleases(new Date(0), new Date());
        int pivot = releases.size() - (int)(Math.floor((lastReleasePercentageToIgnore) / 100.0 * releases.size()));
        releases = releases.subList(0, pivot);

        // prepares observation matrix with a row for each release and a column for each resource. Matrix cells are initialized with empty Observation objects
        observationMatrix = new ObservationMatrix(releases.toArray(new Release[0]));
        observationMatrix.getMatrix().forEach(( releaseName, observations) -> {
            try {
                List<String> fileNamesOfRelease = gitDao.getFileNamesOfRelease(releaseName);
                for (String fileName : fileNamesOfRelease){
                    observations.put(fileName, new EnumMap<>(Feature.class));
                }
            } catch (UnableToGetFileNamesException e) {
                String msg = String.format("ignoring files of release %s", releaseName);
                logger.log(Level.WARNING, msg, e);
            }
        });

        // launches miners
        mineDataBean = new MineDataBean();
        mineDataBean.setObservationMatrix(observationMatrix);

        try (CSVPrinter printer = new CSVPrinter(new FileWriter("out.csv"), CSVFormat.DEFAULT)) {

            Set<Feature> orderedFeatures = new LinkedHashSet<>(featuresToMeasure);
            
            printer.print("Release");
            printer.print("Resource");
            for (Feature f : orderedFeatures){
             //   if (f != Feature.BUGGYNESS){
                    printer.print(f.getName());
            //    }
            }
            //printer.print(Feature.BUGGYNESS.getName());
            printer.println();
            
            for (Release r : releases) { // for each release ...
                String progressMsg = String.format("Mined %d%% of releases", releases.indexOf(r) * 100 / releases.size());
                logger.log(Level.INFO, progressMsg);
                mineDataBean.setRelease(r);
                mineDataBean
                        .getObservationMatrix()
                        .getMatrix()
                        .get(r.getName()) // ... get all observations at given release ...
                        .forEach((resourceName, observation) -> { // ... and for each resource at given release ...
                            mineDataBean.setResourceName(resourceName);
                            for (Miner m : miners) { // ... mine all features of given resources.
                                m.mine(mineDataBean); // Mined features will be stored in corresponding observation matrix cells
                            }
                            try {
                                printer.print(mineDataBean.getRelease().getName());
                                printer.print(mineDataBean.getResourceName());
                                for (Feature f : orderedFeatures){
                  //                  if (f != Feature.BUGGYNESS{
                                        printer.print(
                                            mineDataBean
                                            .getObservationMatrix()
                                            .getMatrix()
                                            .get(mineDataBean.getRelease().getName())
                                            .get(mineDataBean.getResourceName())
                                            .get(f)
                                        );
               //                     }
     /*                               printer.print(printer.print(
                                        mineDataBean
                                        .getObservationMatrix()
                                        .getMatrix()
                                        .get(mineDataBean.getRelease().getName())
                                        .get(mineDataBean.getResourceName())
                                        .get(Feature.BUGGYNESS));
                                        })
                                */
                                printer.println();
                                    }
                            } catch (IOException e) {
                                String msg = String.format("unable to append line to csv of %s at %s", mineDataBean.getResourceName(), mineDataBean.getRelease().getName());
                                logger.log(Level.SEVERE, msg, e);
                            }
                        });
                        printer.flush();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "unable to close csv file", e);
        }
    }
}
