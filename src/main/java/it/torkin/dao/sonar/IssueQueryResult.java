package it.torkin.dao.sonar;

public class IssueQueryResult {
    
    private int total;                      // # of issues.
    private int p;                          // current page of results
    private int ps;                         // # of issues per pages

    public int getTotal() {
        return total;
    }
    public void setTotal(int total) {
        this.total = total;
    }
    public int getP() {
        return p;
    }
    public void setP(int p) {
        this.p = p;
    }
    public int getPs() {
        return ps;
    }
    public void setPs(int ps) {
        this.ps = ps;
    }

    
}
