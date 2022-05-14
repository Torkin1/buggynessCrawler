package it.torkin.dao.jira;

public class IssueQueryResult extends JiraQueryResult{
    
    private JiraIssue[] issues;     // issues retrieved in this result page

    public JiraIssue[] getIssues() {
        return issues;
    }

    public void setIssues(JiraIssue[] issues) {
        this.issues = issues;
    }

    
}
