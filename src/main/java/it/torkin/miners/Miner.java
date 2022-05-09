package it.torkin.miners;

public abstract class Miner {
    
    /**
     * mines features specified in bean for a given resource in a given release.
     * @param bean
     */
    public abstract void mine(MineDataBean bean);

    protected String owner;
    protected String repo;
    
    protected Miner(String owner, String project){
        this. owner = owner;
        this.repo = project;
    }
}
