package it.torkin.dao.jira;

public class IssueQueryResult {
    
    private int startAt;            // start index of result page
    private int maxResults;         // how many elements this page result contains
    private int total;              // how many elements are in the result
    private JiraIssue[] issues;     // issues retrieved in this result page

    public int getStartAt() {
        return startAt;
    }

    public void setStartAt(int startAt) {
        this.startAt = startAt;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public JiraIssue[] getIssues() {
        return issues;
    }

    public void setIssues(JiraIssue[] issues) {
        this.issues = issues;
    }

    
}
