package it.torkin.miners;

import java.util.EnumSet;

public enum Feature {
    
    /**
     * 1) **publics: num of public methods and attributes.
     * L'idea è che una classe con tanti metodi e attributi pubblici comunica molto con il resto del sistema,
     * e dunque offre più superficie per la comparsa di malfunzionamenti durante interazione.
     * Inoltre, una classe accessibile da più punti è meno mantenibile rispetto a una classe
     * accessibile solo in pochi, conosciuti modi.
     * Unit: "public " keyword occurrences 
     */
    PUBLICS("publics", PublicsMiner.class),
    
    /**
     * 2) **NSmells**: Un alto numero di smells indica una difficile manutenzione
     * Unit: smells
     */
    CODE_SMELLS("codeSmells", CodeSmellsMiner.class),
    
    /**
     * 3) **Age**: Più una classe è vecchia e meno è probabile che
     * vengano scovati nuovi bug
     * Unit: weeks 
     * 
     */
    AGE("age", AgeMiner.class),
    
    
    /**
     * 4) **NAuth**: Più persone lavorano a una classe e più è probabile
     * che sorgano errori dovuti a equivoci, incomprensioni, cambi di stile, ...
     * Unit: num of authors
     */
    N_AUTH("nAuth", AuthorsMiner.class),
    
    /**
     * 5) **Size**: Più è grande, più è probabile che contenga bugs
     * Unit: non-empty LOCs
     */
    SIZE("size", SizeMiner.class),
    
    /**
     * 6) **NR**: in combinazione con age, se una classe è stata toccata poco
     * in un periodo lungo di tempo, è probabile che sia esente da bugs
     * Unit: revisions
     */
    N_R("nr", NrMiner.class),
    
    /**
     * 7) **NFix**: se classe è fixata in continuazione
     * è probabile che sia molto prona a errori
     * Unit: fix commits at given release with the target resource in it's changeset 
     * 
     */
    N_FIX("nFix", FixMiner.class),
    
     /**
     * 8) **Churn**: Più la classe è stata modificata nella release considerata
     *  più è probabile che vengano introdotti dei bugs a seguito delle modifiche
     * Unit: LOCs (added - deleted)
     */
    CHURN("churn", ChurnMiner.class),
    
    /**
     * 9) **dependencies**: Una classe che dipende da tante altre classi
     * è più probabile che sia soggetta a regressioni di altre
     * Unit: total occurrences of keywords among "import ", "extends ", "implements " 
     */
    DEPENDENCIES("dependencies", DependenciesMiner.class),

    /** A resource is buggy at a given release if such release is among fixed version of a resolved bug ticket
     * and the resource is mentioned in the changeset of the related fix commit
     * Unit: yes/no 
    */
    BUGGYNESS("isBuggy", null)
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
