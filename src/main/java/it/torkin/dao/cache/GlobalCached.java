package it.torkin.dao.cache;

public enum GlobalCached {
    
    RELEASES("releases"),
    FIXED_BUGS("fixedBugs"),
    CODE_SMELLS("codeSmells");
    
    ;

    private final String key;
    
    private GlobalCached(String key){
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
