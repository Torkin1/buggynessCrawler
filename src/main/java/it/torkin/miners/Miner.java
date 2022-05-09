package it.torkin.miners;

public interface Miner {
    
    /**
     * mines features specified in bean for a given resource in a given release.
     * @param bean
     */
    public void mine(MineDataBean bean);
}
