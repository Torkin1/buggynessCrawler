package it.torkin.miners;

import java.util.logging.Level;
import java.util.logging.Logger;

import it.torkin.dao.sonar.SonarDao;
import it.torkin.dao.sonar.UnableToGetSmellsException;

public class CodeSmellsMiner extends Miner{

    private Logger logger = Logger.getLogger(this.getClass().getName());
    /** sonar organization name */
    private String organization;
    /** sonar project name */
    private String project;
    
    public CodeSmellsMiner(String organization, String project){
        super(organization, project);
        this.organization = super.owner.toLowerCase();
        this.project = String.format("%s%s_%s", super.owner.substring(0,1).toUpperCase(), super.owner.substring(1), super.repo);
    }
    
    @Override
    public void mine(MineDataBean bean) {

        try {
            
            // queries sonarcloud for smells of given file at given release
            SonarDao dao = new SonarDao(this.organization, this.project);
            int nSmells = dao
                .getCodeSmells(bean.getResourceName(), bean.getTimeOrderedReleases().get(bean.getReleaseIndex())
                .getReleaseDate())
                .size();
            bean
                .getObservationMatrix()
                .getMatrix()
                .get(bean.getTimeOrderedReleases().get(bean.getReleaseIndex()).getName())
                .get(bean.getResourceName())
                .put(Feature.CODE_SMELLS, Integer.toString(nSmells));
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
