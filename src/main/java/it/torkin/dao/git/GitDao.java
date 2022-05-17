package it.torkin.dao.git;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.MessageRevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import it.torkin.entities.Release;

public class GitDao {
    
    private final Repository repository;
    private static final String REPO_DIR_NAME = "repos";

    public GitDao(String repoName) throws UnableToAccessRepositoryException{
        try {
            FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
            repositoryBuilder.setMustExist(true);
            repositoryBuilder.setWorkTree(new File(REPO_DIR_NAME + File.separator + repoName));
            this.repository = repositoryBuilder.build();
        } catch (IOException e) {
            throw new UnableToAccessRepositoryException(repoName, e);            
        }
    }

    public Repository getRepository() {
        return repository;
    }

    public List<String> getFileNames(RevCommit commit) throws UnableToGetFileNamesException{
        List<String> fileNames = new ArrayList<>();

        String currentResourceName;
        try (TreeWalk treeWalk = new TreeWalk(repository); ){
            RevTree commitTree = commit.getTree(); // tree of files at given commit

            // will walk files at given tree
                                          
            treeWalk.addTree(commitTree);
            treeWalk.setRecursive(true); // enters in directories
            treeWalk.setFilter(PathSuffixFilter.create(".java")); // only java files listed
            while(treeWalk.next()){
                currentResourceName = treeWalk.getPathString();
                if (!isTest(currentResourceName)){
                    fileNames.add(currentResourceName);
                }
            }
        } catch (IOException e) {
            
            throw new UnableToGetFileNamesException(e);
        }

        return fileNames;

    }

    /**
     * gets most recent commit applied before beforeDate containing optional commentContent string in its comment 
     * 
     * @return
     * @throws UnableToGetCommitsException
     */
    public RevCommit getLatestCommit(Date beforeDate, String commentContent) throws UnableToGetCommitsException {

        try (Git git = new Git(repository)) {

            LogCommand logCommand = git.log();
            
            if (commentContent != null){
                logCommand.setRevFilter(MessageRevFilter.create(commentContent));
            }
            Iterable<RevCommit> commits = logCommand.call();
            for (RevCommit commit : commits){
                if (new Date (commit.getCommitTime()).compareTo(beforeDate) < 0){
                    return (commit);
                }
            }
            return null;

        } catch (GitAPIException e) {

            throw new UnableToGetCommitsException(e);
        }
    }

    public RevCommit getLatestCommit(Date beforeDate) throws UnableToGetCommitsException {
    
        return getLatestCommit(beforeDate, null);
    }

    private boolean isTest(String resourceName){
        return resourceName.contains("test") || resourceName.contains("Test");
    }
    
    /**This method does not work with the first commit ever of the repository, because it has no parent */
    public Set<String> getCommitChangeSet(RevCommit commit) throws UnableToGetChangeSetException{
        List<DiffEntry> diffEntries;
        Set<String> names = new HashSet<>();
        String name;

        try (   
            RevWalk rw = new RevWalk(repository);
            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        ){

            RevCommit parent = commit.getParent(0);
            if (parent == null){
                throw new NullParentException();
            } 
            df.setRepository(repository);
            df.setDiffComparator(RawTextComparator.DEFAULT);
            df.setPathFilter(PathSuffixFilter.create(".java"));
            diffEntries = df.scan(parent.getTree(), commit.getTree());

            for (DiffEntry entry : diffEntries){    // get path for each touched files
                name = entry.getOldPath();
                if (entry.getChangeType().equals(ChangeType.MODIFY) && !isTest(name)){
                    names.add(name);
                }
            }
            return names;
            
        } catch (IOException | NullParentException e) {
            throw new UnableToGetChangeSetException(e);
        }
    }

    public void checkoutRelease(Release release) throws UnableToCheckoutReleaseException{

        try (Git git = new Git(this.repository)){
            RevCommit releaseCommit = getLatestCommit(release.getReleaseDate());
            git.checkout().setName(releaseCommit.getName()).call();
            
        } catch (UnableToGetCommitsException | GitAPIException e) {
            
            throw new UnableToCheckoutReleaseException(e);
        }

    }

    public File getFile(String fileName) throws FileNotFoundException{
        String prefix = REPO_DIR_NAME + File.separator + repository.getWorkTree().getName() + File.separator;
        File target = new File(prefix + fileName);
        if(!target.exists()){
            throw new FileNotFoundException(fileName);
        }
        return target;
    }
}
