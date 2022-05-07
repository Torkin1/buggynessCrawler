package it.torkin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import it.torkin.entities.Release;

public class App 
{
    private static Logger logger = Logger.getLogger(App.class.getName());
    private static final String REPO_URL_TEMPLATE = "https://github.com/%s/%s";
    
    public static void main( String[] args ) throws InvalidArgumentsException, UnableToAccessRepositoryException
    {
        if (args.length < 2){
            throw new InvalidArgumentsException("Not enough arguments. Please specify repo author and name (in this order)");
        }
        
        String[] repoString = args[0].split("/");                       // repoAuthor/repoName
        int lastReleasePercentageToIgnore = Integer.parseInt(args[1]);  // What percentage of most recent release we ignore to mitigate snoring
        
        String repoAuthor = repoString[0];
        String repoName = repoString[1]; 
        Repository repository = null;
        File repoDir = new File(repoName);

        // Opens local repo if exists.       
        do {
            try {
            
                FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
                repositoryBuilder.setMustExist(true);
                repositoryBuilder.setWorkTree(repoDir);
                repository = repositoryBuilder.build();
            } catch (IOException e) {
                
                // Local repo is not accessible, we try to clone it.
                logger.log(Level.WARNING, "Local repo is not accessible, cloning it");
                String repoUri = String.format(REPO_URL_TEMPLATE, repoAuthor, repoName);
                try {
                    Git.cloneRepository().setURI(repoUri).setDirectory(repoDir).call();
                } catch (GitAPIException e1) {
                    
                    throw new UnableToAccessRepositoryException("repo doesn't exits locally and can't clone it", e1);
                }
            }
    
        } while (repository == null);
        String repoOpenedMsg = String.format("repo %s/%s successfully opened", repoAuthor, repoName);
        logger.log(Level.INFO, repoOpenedMsg);

        // extracts list of releases and makes it global accessible
        Git git = new Git(repository);
        List<Release> releases = new ArrayList<>();
        try {
            List<Ref> tags = git.tagList().call();
            final RevWalk walk = new RevWalk(repository);

            // converts every git tag in a Release object
            for (Ref tag : tags){
                Release release = new Release();
                release.setName(tag.getName());
                release.setReleaseDate(walk.parseTag(tag.getObjectId()).getTaggerIdent().getWhen());
                releases.add(release);
            }

            // orders releases by release date
            Collections.sort(releases, (r1, r2) -> {

                return r1.getReleaseDate().compareTo(r2.getReleaseDate());
            });

            // thrash away most recent releases
            int pivot = (lastReleasePercentageToIgnore / 100 * tags.size()) - 1;
            releases = releases.subList(0, pivot);

            // saves releases in global accessible object
            TargetInformations.getReference().getReleases().addAll(releases);
        
        } catch (GitAPIException | IOException e) {
            
            logger.log(Level.SEVERE, e.getMessage());
        }
  

    }
}
