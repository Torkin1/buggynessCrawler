package it.torkin.dao.cache;

public class GlobalCacheHolder {
    
    // singleton logic
    private static GlobalCacheHolder ref = null;
    
    protected GlobalCacheHolder(){}

    public Cache getCache() {
        return cache;
    }

    public static GlobalCacheHolder getRef(){
        if (ref == null){
            ref = new GlobalCacheHolder();
        }

        return ref;
    }

    private final Cache cache = new Cache();

}
