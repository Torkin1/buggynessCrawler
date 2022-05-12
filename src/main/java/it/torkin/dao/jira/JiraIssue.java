package it.torkin.dao.jira;

public class JiraIssue {

    private String id;                  // id of issue
    private String key;                 // key of issues (same used in commit comments)
    private JiraIssueFields fields;     // fields of jira issue 

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public JiraIssueFields getFields() {
        return fields;
    }
    public void setFields(JiraIssueFields fields) {
        this.fields = fields;
    }

    
}
