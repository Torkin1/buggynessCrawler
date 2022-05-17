package it.torkin.dao.jira;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import it.torkin.dao.cache.GlobalCacheHolder;
import it.torkin.dao.cache.GlobalCached;
import it.torkin.rest.ClientResourceGetter;
import it.torkin.rest.UnableToGetResourceObjectException;

public class JiraDao {

    private enum Query{
        GET_ALL_FIXED_BUGS("https://issues.apache.org/jira/rest/api/2/search?jql=project='%s'AND'issueType'='Bug'AND('status'='closed'OR'status'='resolved')AND'resolution'='fixed'ORDER BY'created'ASC&fields=created,fixVersions,versions,resolutiondate,created&startAt=%d"),
        GET_ALL_RELEASES("https://issues.apache.org/jira/rest/api/2/project/%s/version")
        ;
    
        private Query(String queryString) {
            this.queryString = queryString;
        }

        private final String queryString;

        public String getQueryString() {
            return queryString;
        }
    
    }
    
    private String jiraProject;

    public JiraDao(String jiraProject){
        this.jiraProject = jiraProject;
    }

    private String forgeQuery(Query query, Object... args){
        return String.format(query.getQueryString(), args);
    }

    /**
     * gets all released versions
     * 
     * @throws UnableToGetReleasesException
     */
    public List<JiraRelease> getAllReleased() throws UnableToGetReleasesException {

        List<JiraRelease> released = new ArrayList<>();
        if (GlobalCacheHolder.getRef().getCache().getCached().get(GlobalCached.RELEASES.getKey()) == null) {
            try {
                List<JiraRelease> buffer = new ArrayList<>();
                ReleaseQueryResult queryResult;
                int start = 0;
                do {
                    queryResult = new ClientResourceGetter<>(ReleaseQueryResult.class)
                            .getClientResourceObject(forgeQuery(Query.GET_ALL_RELEASES, this.jiraProject));
                    for (JiraRelease r : queryResult.getValues()) {
                        if (r.isReleased() && r.getReleaseDate() != null) {
                            buffer.add(r);
                        }
                    }
                    start += queryResult.getValues().length;
                } while (start < queryResult.getTotal());
                GlobalCacheHolder.getRef().getCache().getCached().put(GlobalCached.RELEASES.getKey(), buffer);
            } catch (UnableToGetResourceObjectException e) {

                throw new UnableToGetReleasesException(e);
            }
        }
        released.addAll((List<JiraRelease>) GlobalCacheHolder.getRef().getCache().getCached().get(GlobalCached.RELEASES.getKey()));
        return released;
    }

    public List<JiraIssue> getTimeOrderedFixedBugIssues() throws UnableToGetAllFixedBugsException {

        List<JiraIssue> fixedBugIssues = new ArrayList<>();
        
        if (GlobalCacheHolder.getRef().getCache().getCached().get(GlobalCached.FIXED_BUGS.getKey()) == null) {
            List<JiraIssue> buffer = new ArrayList<>();
            // query fixed bug issues from server
            try {
                int start = 0;
                IssueQueryResult queryResult;
                do {
                    queryResult = new ClientResourceGetter<>(IssueQueryResult.class)
                            .getClientResourceObject(forgeQuery(Query.GET_ALL_FIXED_BUGS, this.jiraProject, start));
                    buffer.addAll(Arrays.asList(queryResult.getIssues()));
                    start += queryResult.getIssues().length;

                } while (start < queryResult.getTotal());
                GlobalCacheHolder.getRef().getCache().getCached().put(GlobalCached.FIXED_BUGS.getKey(), buffer);

            } catch (UnableToGetResourceObjectException e) {

                throw new UnableToGetAllFixedBugsException(e);
            }
        }
        fixedBugIssues.addAll(((List<JiraIssue>) GlobalCacheHolder.getRef().getCache().getCached().get(GlobalCached.FIXED_BUGS.getKey())));
        return fixedBugIssues;
    }

    /** will get fixed bugs with fv version included between startFv and endFv dates, both included */
    public List<JiraIssue> getTimeOrderedFixedBugIssues(Date startFvDate, Date endFvDate) throws UnableToGetAllFixedBugsException{

        List<JiraIssue> issues = this.getTimeOrderedFixedBugIssues();
        issues.removeIf(i -> {
            JiraRelease fv = null;
            JiraRelease[] fixVersions = i.getFields().getFixVersions();
            if (fixVersions.length == 0){
                return true;
            }
            else{
                for (JiraRelease r : fixVersions){
                    if (r.getReleaseDate() != null){
                        fv = r;
                        break;
                    }
                }
            }
            return fv == null || (fv.getReleaseDate().compareTo(startFvDate) < 0 ||  fv.getReleaseDate().compareTo(endFvDate) > 0); 
        });
        return issues;
    }

    public List<JiraRelease> getAllReleased(Date startDate, Date endDate) throws UnableToGetReleasesException{
        List<JiraRelease> releases = getAllReleased();
        releases.removeIf(r -> r.getReleaseDate().compareTo(startDate) < 0 || r.getReleaseDate().compareTo(endDate) >= 0);
        return releases;
    }

    public int getReleaseOrderIndex(JiraRelease release) throws UnableToGetReleasesException, UnknownJiraReleaseException{

        List<JiraRelease> released = getAllReleased();
        for (int i = 0; i < released.size(); i ++){
            if (released.get(i).getName().compareTo(release.getName()) == 0){
                return i;
            }
        }
        throw new UnknownJiraReleaseException(release.getName());
    }


    
}
