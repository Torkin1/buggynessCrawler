package it.torkin.dao.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

import it.torkin.UnableToAccessRepositoryException;
import it.torkin.entities.Release;

public class GitDao {
    
    private final Repository repository;

    public GitDao(String repoName) throws UnableToAccessRepositoryException{
        try {
            FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
            repositoryBuilder.setMustExist(true);
            repositoryBuilder.setWorkTree(new File(repoName));
            this.repository = repositoryBuilder.build();
        } catch (IOException e) {
            throw new UnableToAccessRepositoryException(repoName, e);            
        }
    }

    public Repository getRepository() {
        return repository;
    }

    public List<Release> getReleases(Date startDate, Date endDate) throws UnableToGetReleasesException {
        Git git = new Git(repository);
        List<Release> releases = new ArrayList<>();
        try {
            List<Ref> tags = git.tagList().call();
            RevWalk walk = new RevWalk(repository);
            // converts every git tag in a Release object
            Date currentTagDate;
            Release currentRelease;
            RevObject currentObject;
            for (Ref tag : tags) {

                currentObject = walk.parseAny(tag.getObjectId());
                if (currentObject instanceof RevTag){
                    currentTagDate = ((RevTag) currentObject).getTaggerIdent().getWhen();
                    if (currentTagDate.compareTo(startDate) >= 0 && currentTagDate.compareTo(endDate) <= 0) {
                        currentRelease = new Release();
                        currentRelease.setName(tag.getName());
                        currentRelease.setReleaseDate(currentTagDate);
                        releases.add(currentRelease);
                    }

                        
                }
                
            }

            return releases;

        } catch (GitAPIException | IOException e) {

            throw new UnableToGetReleasesException(e);
        }
    }
    
    public List<Release> getTimeOrederedReleases(Date startDate, Date endDate) throws UnableToGetReleasesException {
        List<Release> releases = this.getReleases(startDate, endDate);

        // orders releases by release date
        Collections.sort(releases, (r1, r2) -> r1.getReleaseDate().compareTo(r2.getReleaseDate()));

        return releases;
    }

    public List<String> getFileNamesOfRelease(String releaseName) throws UnableToGetFileNamesException{
        List<String> fileNames = new ArrayList<>();

        RevWalk revWalk = new RevWalk(repository);
        String currentResourceName;
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(repository.resolve(releaseName).getName()));      // gets commit corresponding to release name
            RevTree commitTree = revCommit.getTree();                                                                             // tree of files at given commit

            // will walk files at given tree
            TreeWalk treeWalk = new TreeWalk(repository);                               
            treeWalk.addTree(commitTree);
            treeWalk.setRecursive(true);                                                                                        // enters in directories
            treeWalk.setFilter(PathSuffixFilter.create(".java"));                                                           // only java files listed
            while(treeWalk.next()){
                currentResourceName = treeWalk.getPathString();
                if (!currentResourceName.contains("test") && !currentResourceName.contains("Test")){
                    fileNames.add(currentResourceName);
                }
            }
        } catch (IOException e) {
            
            throw new UnableToGetFileNamesException(releaseName, e);
        }

        return fileNames;

    }
}
