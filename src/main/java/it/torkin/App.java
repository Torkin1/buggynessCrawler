package it.torkin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

/**
 * Hello world!
 *
 */
public class App 
{
    private static Logger logger = Logger.getLogger(App.class.getName());
    public static void main( String[] args ) throws InvalidArgumentsException, UnableToAccessRepositoryException
    {
        if (args.length < 2){
            throw new InvalidArgumentsException("Not enough arguments. Please specify repo author and name (in this order)");
        }
        
        String repoAuthor = args[0];
        String repoName = args[1]; 
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
                String repoUri = String.format("https://github.com/%s/%s", repoAuthor, repoName);
                try {
                    Git.cloneRepository().setURI(repoUri).setDirectory(repoDir).call();
                } catch (GitAPIException e1) {
                    
                    throw new UnableToAccessRepositoryException("repo doesn't exits locally and can't clone it", e1);
                }
            }
    
        } while (repository == null);
        logger.log(Level.INFO, String.format("repo %s/%s successfully opened", repoAuthor, repoName));

    }
}
