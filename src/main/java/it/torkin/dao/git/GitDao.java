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
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.filter.AndRevFilter;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.revwalk.filter.MessageRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
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

    /**gets names of all files in work tree at given commit */
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
     * gets most recent commit applied strictly before {@code beforeDate}  containing optional {@code commentContent} string in its comment 
     * 
     * @throws UnableToGetCommitsException
     */
    public RevCommit getLatestCommit(Date beforeDate, String commentContent) throws UnableToGetCommitsException {

        try (Git git = new Git(repository)) {

            LogCommand logCommand = git.log();
            RevFilter filter;
            
            // set filter on commit time and comment content
            filter = CommitTimeRevFilter.before(beforeDate);
            if (commentContent != null){
                filter = AndRevFilter.create(filter, MessageRevFilter.create(commentContent));
            }
            logCommand.setRevFilter(filter);

            // extracts latest commit which matches filter
            Iterable<RevCommit> commits = logCommand.call();
            RevCommit latest = null;
            Date candidateDate = new Date(0);
            for (RevCommit candidate : commits){
                candidateDate = candidate.getCommitterIdent().getWhen();
                if (candidateDate.compareTo(beforeDate) < 0 && (latest == null || candidateDate.compareTo(latest.getCommitterIdent().getWhen()) > 0)){
                    latest = candidate;
                }
            }
            return latest;

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
    
    /**
     * gets diff for every file changed between oldCommit and newCommit
     * @param oldCommit
     * @param newCommit
     * @return
     * @throws UnableToDoDiffException
     */
    public List<DiffEntry> getDiffs(RevCommit oldCommit, RevCommit newCommit) throws UnableToDoDiffException{
        List<DiffEntry> diffEntries;
        try (   
            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            ObjectReader reader = repository.newObjectReader();
        ){

            AbstractTreeIterator oldTreeIterator = (oldCommit != null)? new CanonicalTreeParser(null, reader, oldCommit.getTree()) : new EmptyTreeIterator();
            AbstractTreeIterator newTreeIterator = new CanonicalTreeParser(null, reader, newCommit.getTree());
            df.setRepository(repository);
            df.setDiffComparator(RawTextComparator.DEFAULT);
            df.setPathFilter(PathSuffixFilter.create(".java"));
            diffEntries = df.scan(oldTreeIterator, newTreeIterator);
            return diffEntries;

            
        } catch (IOException e) {
            throw new UnableToDoDiffException(e);
        }

    }
    
    /**Gets all names of files in current work tree modified by given commit, in respect to the parent commit.
     * This method does not work with the first commit ever of the repository, because it has no parent
     * You can switch work tree calling a checkout first */
    public Set<String> getCommitChangeSet(RevCommit commit) throws UnableToGetChangeSetException{
        List<DiffEntry> diffEntries;
        Set<String> names = new HashSet<>();
        String name;

        try {
            
            RevCommit parent = commit.getParent(0);
            diffEntries = getDiffs(parent, commit);

            for (DiffEntry entry : diffEntries){    // get path for each touched files
                name = entry.getOldPath();
                if (entry.getChangeType().equals(ChangeType.MODIFY) && !isTest(name)){
                    names.add(name);
                }
            }
            return names;
            
        } catch (UnableToDoDiffException e) {
            throw new UnableToGetChangeSetException(e);
        }
    }


    public void checkout(RevCommit commit) throws UnableToCheckoutReleaseException{
        try (Git git = new Git(this.repository)){
            git.checkout().setName(commit.getName()).call();
            
        } catch (GitAPIException e) {
            
            throw new UnableToCheckoutReleaseException(e);
        }
    }
    
    /**checkouts local clone at the latest commit prior to given release */
    public void checkout(Release release) throws UnableToCheckoutReleaseException{

        try (Git git = new Git(this.repository)){
            RevCommit releaseCommit = getLatestCommit(release.getReleaseDate());
            checkout(releaseCommit);
            
        } catch (UnableToGetCommitsException e) {
            
            throw new UnableToCheckoutReleaseException(e);
        }

    }
    
    /**checkouts local clone to master 
     * @throws UnableToCheckoutReleaseException
     * */
    public void checkout() throws UnableToCheckoutReleaseException{
        try (Git git = new Git(this.repository)){
            checkout("master");         
        }
    }

    public void checkout(String name) throws UnableToCheckoutReleaseException{
        try (Git git = new Git(this.repository)){
            git.checkout().setName(name).call();
            
        } catch (GitAPIException e) {
            
            throw new UnableToCheckoutReleaseException(e);
        } 
    }

    /** gets file in repository clone in local file system given it's path relative to work tree.
     * @param fileName path of requested file relative to repo work tree
     * @return File object representing same file in local file system coordinates (relative to {@code REPO_DIR_NAME})
     */
    public File getFile(String fileName) throws FileNotFoundException{
        String prefix = REPO_DIR_NAME + File.separator + repository.getWorkTree().getName() + File.separator;
        File target = new File(prefix + fileName);
        if(!target.exists()){
            throw new FileNotFoundException(fileName);
        }
        return target;
    }

/** gets all commits related to given file */
public List<RevCommit> getAllCommits(String fileName) throws UnableToGetCommitsException{
    List<RevCommit> commitList = new ArrayList<>();
    try(Git git = new Git(repository)){
        Iterable<RevCommit> commits = git.log().addPath(fileName).call();
        commits.forEach(commitList::add);
    } catch (GitAPIException e) {
        throw new UnableToGetCommitsException(e);
    }
    return commitList;
}

/** gets all commits related to given file and committed strictly before given date */
public List<RevCommit> getAllCommits(String fileName, Date endDate) throws UnableToGetCommitsException{
    return getAllCommits(fileName, new Date(0), endDate);
}

/** gets all commits related to given file and committed in given date range (end date excluded) */
public List<RevCommit> getAllCommits(String fileName, Date startDate, Date endDate) throws UnableToGetCommitsException{
    List<RevCommit> commits = getAllCommits(fileName);
    commits.removeIf(commit ->
        commit.getCommitterIdent().getWhen().compareTo(startDate) < 0 || commit.getCommitterIdent().getWhen().compareTo(endDate) >= 0
    
            );
    return commits;

}
    /**
     * gets oldest commit of given resource
     * 
     * @throws UnableToGetCommitsException
     */
    public RevCommit getOldestCommit(String resourceName) throws UnableToGetCommitsException {
        // find oldest commit of target resource
        List<RevCommit> commits = getAllCommits(resourceName);
        RevCommit oldest = commits.get(0);
        for (RevCommit candidate : commits) {
            if (candidate.getCommitTime() < oldest.getCommitTime()) {
                oldest = candidate;
            }
        }
        return oldest;
    }
}
