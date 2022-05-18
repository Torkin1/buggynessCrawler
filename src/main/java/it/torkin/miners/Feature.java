package it.torkin.miners;

import java.util.EnumSet;

public enum Feature {
    
    /**
     * 1) **publics: num of public methods and attributes.
     * L'idea è che una classe con tanti metodi e attributi pubblici comunica molto con il resto del sistema,
     * e dunque offre più superficie per la comparsa di malfunzionamenti durante interazione.
     * Inoltre, una classe accessibile da più punti è meno mantenibile rispetto a una classe
     * accessibile solo in pochi, conosciuti modi.
     */
    PUBLICS("publics", PublicsMiner.class),
    
    /**
     * 2) **NSmells**: Un alto numero di smells indica una difficile manutenzione 
     */
    CODE_SMELLS("codeSmells", CodeSmellsMiner.class),
    
    /**
     * 3) **Age**: Più una classe è vecchia e meno è probabile che
     * vengano scovati nuovi bug
     * 
     */
    //AGE("age"),
    
    
    /**
     * 4) **NAuth**: Più persone lavorano a una classe e più è probabile
     * che sorgano errori dovuti a equivoci, incomprensioni, cambi di stile, ...
     */
    //N_AUTH("nAuth"),
    
    /**
     * 5) **Size**: Più è grande, più è probabile che contenga bugs
     */
    SIZE("size", SizeMiner.class),
    
    /**
     * 6) **NR**: in combinazione con age, se una classe è stata toccata poco
     * in un periodo lungo di tempo, è probabile che sia esente da bugs
     */
    //N_R("nr"),
    
    /**
     * 7) **NFix**: se classe è fixata in continuazione
     * è probabile che sia molto prona a errori
     * 
     */
    //N_FIX("nFix"),
    
     /**
     * 8) **LOCs_touched**: Più percentuale di classe è stata modificata, più è probabile che
     * vengano introdotti dei bugs a seguito delle modifiche
     */
    //LOC_TOUCHED("locTouched"),
    
    /**
     * 9) **imports**: Una classe che dipende da tante altre classi
     * è più probabile che sia soggetta a regressioni di altre
     */
    IMPORTS("imports", ImportsMiner.class),

    BUGGYNESS("buggyness", null)
    ;

    private final String name;
    private final Class<? extends Miner> miner;
    
    private Feature(String name, Class <? extends Miner> miner){
        this.name = name;
        this.miner = miner;
    }

    public static Feature getFeatureFromName(String name) throws UnknownFeatureException{
        for (Feature f : EnumSet.allOf(Feature.class)){
            if (name.compareTo(f.getName()) == 0){
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
