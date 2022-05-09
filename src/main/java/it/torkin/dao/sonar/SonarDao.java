package it.torkin.dao.sonar;

import java.text.SimpleDateFormat;
import java.util.Date;

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

    private String forgeProjectIssuesQuery(String resourcePath, SonarIssueType issueType, Date createdBefore){
        
        if (createdBefore == null){
            createdBefore = new Date();
        }  
        String createdBeforeString = new SimpleDateFormat("yyyy-MM-dd").format(createdBefore);
        resourcePath = (resourcePath != null)? String.format(":%s", resourcePath) : "";

        
        return String
            .format(
                SonarQuery.GET_PROJECT_ISSUES.toString(),
                this.organization,
                this.project,
                resourcePath,
                issueType.toString(),
                createdBeforeString
            );
    
    }

    public IssueQueryResult getCodeSmells(String resourcePath, Date createdBefore) throws UnableToGetSmellsException{
        try {
            String query = forgeProjectIssuesQuery(
                        resourcePath,
                        SonarIssueType.CODE_SMELL,
                        createdBefore
                        );
            return (new ClientResourceGetter<IssueQueryResult>(IssueQueryResult.class))
                        .getClientResourceObject(query);
        } catch (UnableToGetResourceObjectException e) {
            throw new UnableToGetSmellsException(e);
        }
        
    }
    
}
