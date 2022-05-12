package it.torkin.dao.jira;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JiraIssueFields {

    private JiraRelease[] versions;     // affected versions
    private JiraRelease[] fixVersions;  // Fix versions
    private Date resolutionDate;        // Closing date
    private Date created;               // Opening Date


    private Logger logger = Logger.getLogger(this.getClass().getName());

    private Date parseDateString(String dateString) throws ParseException{
        return (new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss.SSSZ").parse(dateString));
    }
    
    public JiraRelease[] getFixVersions() {
        return fixVersions;
    }

    public void setFixVersions(JiraRelease[] fixVersions) {
        this.fixVersions = fixVersions;
    }

    public JiraRelease[] getVersions() {
        return versions;
    }
    public void setVersions(JiraRelease[] versions) {
        this.versions = versions;
    }
    public Date getResolutionDate() {
        return resolutionDate;
    }
    public void setResolutionDate(String resolutionDate) {
        try {
            this.resolutionDate = parseDateString(resolutionDate);
        } catch (ParseException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    public Date getCreated() {
        return created;
    }
    public void setCreated(String created) {
        try {
            this.created = parseDateString(created);
        } catch (ParseException e) {
            
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    
}
