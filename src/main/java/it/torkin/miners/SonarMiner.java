package it.torkin.miners;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.torkin.entities.Observation;
import it.torkin.entities.sonarcloud.IssueQueryResult;
import it.torkin.rest.ClientResourceGetter;

public class SonarMiner extends Miner{

    private String organization;
    private String project;
    private Logger logger;
    
    public SonarMiner(String organization, String project){
        super("mine", SonarMiner.class, Feature.CODE_SMELLS);
        this.setOrganization(organization);
        this.setProject(project);
        this.logger = Logger.getLogger(this.getClass().getName());
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

    private String forgeProjectIssuesQuery(SonarQuery query, String resourcePath, SonarIssueType issueType, Date createdBefore){
        
        if (createdBefore == null){
            createdBefore = new Date();
        }  
        String createdBeforeString = new SimpleDateFormat("yyyy-mm-dd").format(createdBefore);
        resourcePath = (resourcePath != null)? String.format(":%s", resourcePath) : "";

        
        return String
            .format(
                query.toString(),
                this.organization,
                this.project,
                resourcePath,
                issueType.toString(),
                createdBeforeString
            );
    
    }
    
    public void mineCodeSmells(Observation observation) {

        
        try {
            // queries sonarcloud for smells of given file at given release
            String query = forgeProjectIssuesQuery(
                    SonarQuery.GET_PROJECT_ISSUES,
                    observation.getResourceName(),
                    SonarIssueType.CODE_SMELL,
                    observation.getRelease().getReleaseDate());
            IssueQueryResult issueQueryResult = (new ClientResourceGetter<IssueQueryResult>(IssueQueryResult.class)).getClientResourceObject(query);
            
            observation.setnSmells(issueQueryResult.getTotal());
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

    }
    
}
