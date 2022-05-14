package it.torkin.dao.jira;

public class ReleaseQueryResult extends JiraQueryResult{
    
    private JiraRelease[] values;     // releases retrieved in this result page

    public JiraRelease[] getValues() {
        return values;
    }

    public void setValues(JiraRelease[] values) {
        this.values = values;
    }
}
