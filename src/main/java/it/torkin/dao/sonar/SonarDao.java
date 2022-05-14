package it.torkin.dao.sonar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import it.torkin.dao.cache.GlobalCacheHolder;
import it.torkin.dao.cache.GlobalCached;
import it.torkin.rest.ClientResourceGetter;
import it.torkin.rest.UnableToGetResourceObjectException;

public class SonarDao {

    private String organization;
    private String project;

    public SonarDao(String organization, String project){
        this.setOrganization(organization);
        this.setProject(project);
    }
    
    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    private String forgeProjectIssuesQuery(String resourcePath, SonarIssueType issueType, int p){
        
        resourcePath = (resourcePath != null)? String.format(":%s", resourcePath) : "";

        
        return String
            .format(
                SonarQuery.GET_PROJECT_ISSUES.toString(),
                this.organization,
                this.project,
                resourcePath,
                issueType.toString(),
                p
            );
    
    }

    public List<SonarIssue> getCodeSmells(String resourcePath, Date createdBefore) throws UnableToGetSmellsException{
        List<SonarIssue> smells = new ArrayList<>();
        if (GlobalCacheHolder.getRef().getCache().getCached().get(GlobalCached.CODE_SMELLS.getKey()) == null) {
            List<SonarIssue> buffer = new ArrayList<>();
            try {
                int p = 1;
                IssueQueryResult result;
                do {
                    String query = forgeProjectIssuesQuery(
                            null,
                            SonarIssueType.CODE_SMELL,
                            p);
                    result = (new ClientResourceGetter<IssueQueryResult>(IssueQueryResult.class))
                            .getClientResourceObject(query);
                    buffer.addAll(Arrays.asList(result.getIssues()));
                    p++;
                } while (p <= (result.getTotal() / result.getPs()) + 1);
                GlobalCacheHolder.getRef().getCache().getCached().put(GlobalCached.CODE_SMELLS.getKey(), buffer);
            } catch (UnableToGetResourceObjectException e) {
                throw new UnableToGetSmellsException(e);
            }
        }
        smells.addAll((List<SonarIssue>)GlobalCacheHolder.getRef().getCache().getCached().get(GlobalCached.CODE_SMELLS.getKey()));
        smells.removeIf(sonarIssue -> {
            return !sonarIssue.getComponent().contains(resourcePath) || sonarIssue.getCreationDate().compareTo(createdBefore) >= 0;
        });
        return smells;
                    
        
    }
    
}
