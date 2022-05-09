package it.torkin.miners;

import java.util.logging.Level;
import java.util.logging.Logger;

import it.torkin.dao.sonar.IssueQueryResult;
import it.torkin.dao.sonar.SonarDao;
import it.torkin.dao.sonar.UnableToGetSmellsException;
import it.torkin.entities.Observation;

public class CodeSmellsMiner implements Miner{

    private Logger logger = Logger.getLogger(this.getClass().getName());
    /** sonar organization name */
    private String organization;
    /** sonar project name */
    private String project;
    
    public CodeSmellsMiner(String organization, String project){
        this.organization = organization;
        this.project = project;
    }
    
    @Override
    public void mine(MineDataBean bean) {

        try {
            
            // queries sonarcloud for smells of given file at given release
            SonarDao dao = new SonarDao(this.organization, this.project);
            IssueQueryResult issueQueryResult = dao.getCodeSmells(bean.getResourceName(), bean.getRelease().getReleaseDate());
            Observation observation = bean
                .getObservationMatrix()
                .getMatrix()
                .get(bean.getRelease().getName())
                .get(bean.getResourceName());
            observation.setnSmells(issueQueryResult.getTotal());
        } catch (UnableToGetSmellsException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    

}
