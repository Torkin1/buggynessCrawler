package it.torkin.dao.jira;

import it.torkin.entities.Release;

public class ReleaseQueryResult extends JiraQueryResult{
    
    private Release[] values;     // releases retrieved in this result page

    public Release[] getValues() {
        return values;
    }

    public void setValues(Release[] values) {
        this.values = values;
    }
}
