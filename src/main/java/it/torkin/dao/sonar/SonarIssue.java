package it.torkin.dao.sonar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SonarIssue {

    private String key;
    private String component;
    private Date creationDate;
    private SonarIssueType type;
    
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getComponent() {
        return component;
    }
    public void setComponent(String component) {
        this.component = component;
    }
    public Date getCreationDate() {
        return creationDate;
    }
    public void setCreationDate(String creationDate) throws ParseException {
        this.creationDate = (new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss.SSSZ").parse(creationDate));
    }
    public SonarIssueType getType() {
        return type;
    }
    public void setType(String type) {
        this.type = SonarIssueType.valueOf(type);
    }

    
    
}
