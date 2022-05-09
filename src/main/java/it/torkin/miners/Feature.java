package it.torkin.miners;

import java.util.EnumSet;

public enum Feature {
    
    CODE_SMELLS("codeSmells", CodeSmellsMiner.class)
    ;

    private final String name;
    private final Class<? extends Miner> miner;
    
    private Feature(String name, Class<? extends Miner> miner){
        this.name = name;
        this.miner = miner;
    }

    public static Feature getFeatureFromName(String name) throws UnknownFeatureException{
        for (Feature f : EnumSet.allOf(Feature.class)){
            if (name.compareTo(f.toString()) == 0){
                return f;
            }
        }
        throw new UnknownFeatureException(name);
    }

    public String getName() {
        return name;
    }

    public Class<? extends Miner> getMiner() {
        return miner;
    }

}
