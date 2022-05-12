package it.torkin.dao.jira;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import it.torkin.rest.ClientResourceGetter;
import it.torkin.rest.UnableToGetResourceObjectException;

public class JiraDao {

    private enum Query{
        GET_ALL_FIXED_BUGS("https://issues.apache.org/jira/rest/api/2/search?jql=project='%s'AND'issueType'='Bug'AND'status'='closed'AND'resolution'='fixed'&fields=created,fixVersions,versions,resolutiondate,created&startAt=%d"),
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

    /**gets all released versions 
     * @throws UnableToGetReleasesException
     * */
    public List<JiraRelease> getAllReleased() throws UnableToGetReleasesException{

        try {
            List<JiraRelease> released = new ArrayList<>();
            ReleaseQueryResult queryResult;
            int start = 0;
            do {
                queryResult = new ClientResourceGetter<>(ReleaseQueryResult.class).getClientResourceObject(forgeQuery(Query.GET_ALL_RELEASES, this.jiraProject));
                for (JiraRelease r : queryResult.getValues()){
                    if (r.isReleased() && r.getReleaseDate() != null){
                        released.add(r);
                    }
                }
                start += queryResult.getValues().length;
            } while (start < queryResult.getTotal());
            return released;
        } catch (UnableToGetResourceObjectException e) {
            
            throw new UnableToGetReleasesException(e);
        }
       
    }

    public List<JiraIssue> getAllFixedBugIssues() throws UnableToGetAllFixedBugsException {

        List<JiraIssue> fixedBugIssues = new ArrayList<>();
        // query fixed bug issues from server
        try {
            int start = 0;
            IssueQueryResult queryResult;
            do {
                queryResult = new ClientResourceGetter<>(IssueQueryResult.class)
                        .getClientResourceObject(forgeQuery(Query.GET_ALL_FIXED_BUGS, this.jiraProject, start));
                fixedBugIssues.addAll(Arrays.asList(queryResult.getIssues()));
                start += queryResult.getIssues().length;

            } while (start < queryResult.getTotal());
            return fixedBugIssues;

        } catch (UnableToGetResourceObjectException e) {

            throw new UnableToGetAllFixedBugsException(e);
        }
    }

    public List<JiraIssue> getAllFixedBugIssues(Date startDate, Date endDate) throws UnableToGetAllFixedBugsException{

        List<JiraIssue> issues = this.getAllFixedBugIssues();
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
            return fv == null || (fv.getReleaseDate().compareTo(startDate) < 0 ||  fixVersions[0].getReleaseDate().compareTo(endDate) > 0); 
        });
        return issues;
    }

    public List<JiraRelease> getAllReleased(Date startDate, Date endDate) throws UnableToGetReleasesException{
        List<JiraRelease> releases = getAllReleased();
        releases.removeIf(r -> r.getReleaseDate().compareTo(startDate) < 0 || r.getReleaseDate().compareTo(endDate) >= 0);
        return releases;
    }


    
}
