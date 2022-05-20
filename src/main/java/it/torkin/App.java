package it.torkin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.jgit.revwalk.RevCommit;

import it.torkin.dao.git.GitDao;
import it.torkin.dao.git.UnableToAccessRepositoryException;
import it.torkin.dao.git.UnableToCheckoutReleaseException;
import it.torkin.dao.git.UnableToGetCommitsException;
import it.torkin.dao.git.UnableToGetFileNamesException;
import it.torkin.dao.jira.JiraDao;
import it.torkin.dao.jira.UnableToGetReleasesException;
import it.torkin.entities.Release;
import it.torkin.entities.ObservationMatrix;
import it.torkin.miners.BuggynessMiner;
import it.torkin.miners.Feature;
import it.torkin.miners.MineDataBean;
import it.torkin.miners.Miner;
import it.torkin.miners.UnableToMineDataException;
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

    /**list of miners of requested implemented features */
    private static List<Miner> miners = new ArrayList<>();

    /** Will hold measures done by miners */
    private static ObservationMatrix observationMatrix;

    /**Releases to get data from */
    private static List<Release> releases;

    private static Map<String, RevCommit> branches = new HashMap<>();
    
    
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
        
        // buggyness must be last feature
        featuresToMeasure.remove(Feature.BUGGYNESS);
    }

    private static void prepareMiners() {
        // Creates miner for desired measures. If a feature miner cannot be found, the
        // feature is removed from resuested features
        for (Feature f : featuresToMeasure) {

            try {
                miners.add(f.getMiner().getConstructor(String.class, String.class).newInstance(repoOwner, repoName));
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                String msg = String.format("Unable to find implementation %s to mine feature %s",
                        f.getClass().getSimpleName(), f.getName());
                logger.log(Level.WARNING, msg, e);
                featuresToMeasure.remove(f);
            }
        }

    }

    private static void prepareReleases() throws UnableToPrepareReleasesException{
        
        // gets list of releases to mine data from, discarding desired last releases percentage
        try {
            JiraDao jiraDao = new JiraDao(repoName.toUpperCase());
            releases = jiraDao.getAllReleased();
            int pivot = releases.size() - (int)(Math.floor((lastReleasePercentageToIgnore) / 100.0 * releases.size()));
            releases = releases.subList(0, pivot);
        } catch (UnableToGetReleasesException e) {
            throw new UnableToPrepareReleasesException(e);
        }

    }
    
    private static void prepareObservationMatrix() throws UnableToPrepareObservationMatrixException{

        try {
            GitDao gitDao = new GitDao(repoName);
            
            // prepares observation matrix with a row for each release and a column for each resource. Matrix cells are initialized with empty Observation objects
            observationMatrix = new ObservationMatrix(releases.toArray(new Release[0]));
                for (Release release : releases){
                    List<String> fileNamesOfRelease = gitDao.getFileNames(gitDao.getLatestCommit(release.getReleaseDate()));
                    for (String fileName : fileNamesOfRelease){
                        Map<Feature, String> observation = new EnumMap<>(Feature.class);
                        observation.put(Feature.BUGGYNESS, "no");
                        observationMatrix.getMatrix().get(release.getName()).put(fileName, observation);
                    }
        }
            
        } catch (UnableToAccessRepositoryException | UnableToGetFileNamesException | UnableToGetCommitsException e) {
            throw new UnableToPrepareObservationMatrixException(e);
        }
            

    }

    private static void prepareBranches() throws UnableToPrepareBranchesException{
        try {
            GitDao gitDao = new GitDao(repoName);
            for (Release r : releases){
                branches.put(r.getName(), gitDao.getLatestCommit(r.getReleaseDate()));
            }
        } catch (UnableToAccessRepositoryException | UnableToGetCommitsException e) {
            throw new UnableToPrepareBranchesException(e);
        }
    }


    private static void printObservationMatrix(MineDataBean mineDataBean, CSVPrinter printer) throws IOException{
        Set<Feature> orderedFeatures = new LinkedHashSet<>(featuresToMeasure);

        // adds buggyness as last feature to print
        orderedFeatures.add(Feature.BUGGYNESS);
        
        // prints headers line in csv file
        printer.print("Release");
        printer.print("Resource");
        for (Feature f : orderedFeatures) {
            printer.print(f.getName());
        }
        printer.println();

        // dumps observations in csv file
        for (Release r : mineDataBean.getTimeOrderedReleases()) {
            String rName = r.getName();
            observationMatrix.getMatrix().get(rName).forEach((fName, observation) -> {
                try {
                    printer.print(rName);
                    printer.print(fName);

                    for (Feature f : orderedFeatures) {
                        printer.print(observation.get(f));
                    }
                    printer.println();
                } catch (IOException e) {

                    logger.log(Level.SEVERE, "unable to write to csv file", e);
                }
            });
        }

    }

    
    public static void main( String[] args ) throws InvalidArgumentsException, UnableToPrepareReleasesException, UnableToPrepareObservationMatrixException, UnableToPrepareBranchesException
    {
        
        // bootstrap operations
        parseArgs(args);

        String outputFileName = String.format("%s_%s_dataset.csv", repoOwner, repoName);
        File outputDir = new File("datasets");
        if (!outputDir.exists()){
            outputDir.mkdir();
        }
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(outputDir.getName() + File.separator + outputFileName), CSVFormat.DEFAULT)) {
                        
            GitDao gitDao = new GitDao(repoName);
            
            logger.info("Bootstraping ...");
            gitDao.checkout();
            prepareMiners();
            prepareReleases();
            prepareObservationMatrix();
            prepareBranches();
            logger.info("Ready to mine!");
            
            // launches miners
            MineDataBean mineDataBean = new MineDataBean();
            mineDataBean.setObservationMatrix(observationMatrix);
            mineDataBean.setTimeOrderedReleases(releases);    
            
            String msg = String.format("about to mine %d releases", releases.size());
            logger.info(msg);
            
            // mines buggyness of all files in release
            mineDataBean.setReleaseIndex(mineDataBean.getTimeOrderedReleases().size() - 1);
            Miner buggynessMiner = new BuggynessMiner(repoOwner, repoName);
            buggynessMiner.mine(mineDataBean);
            logger.info("about to start mining features. This can take a while, why don't you go grab a coffe ☕ in the meantime?");

            for (Release r : releases) { // for each release ...
                mineDataBean.setReleaseIndex(releases.indexOf(r));

                // checkout files at the time of release
                gitDao.checkout(branches.get(r.getName()));

                mineDataBean
                        .getObservationMatrix()
                        .getMatrix()
                        .get(r.getName()) // ... get all observations at given release ...
                        .forEach((resourceName, observation) -> { // ... and for each resource at given release ...
                            try {
                                mineDataBean.setResourceName(resourceName);
                                for (Miner m : miners) { // ... mine all features of given resources.
                                    m.mine(mineDataBean); // Mined features will be stored in corresponding observation matrix cells
                                }
                                
                            } 
                            catch (UnableToMineDataException e) {
                                String msgg = String.format("unable to append line to csv of %s at %s", mineDataBean.getResourceName(), mineDataBean.getTimeOrderedReleases().get(mineDataBean.getReleaseIndex()).getName());
                                logger.log(Level.SEVERE, msgg, e);
                            }
                        });
                                                
                        String progressMsg = String.format("Mined %.2f%% of requested releases", (releases.indexOf(r) + 1) * 100.0 / releases.size());
                        logger.log(Level.INFO, progressMsg);        
            }

           // dumps observation matrix in csv file
            printObservationMatrix(mineDataBean, printer);
            msg = String.format("results available in %s/%s", outputDir.getName(), outputFileName);
            logger.info(msg); 

        } catch (IOException e) {
            logger.log(Level.SEVERE, "unable to write to csv file", e);
        } catch (UnableToAccessRepositoryException | UnableToMineDataException | UnableToCheckoutReleaseException e1) {
            
            logger.log(Level.SEVERE, "unable to mine features", e1);
        }
    }

}
