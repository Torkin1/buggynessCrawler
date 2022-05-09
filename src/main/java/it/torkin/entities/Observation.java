package it.torkin.entities;

/**
 * Collects measures of features of a resource.
 */
public class Observation {
    
    /** resource identifier */
    private String resourceName;    
    /** which releases measures are mined from */
    private Release release;        

    // features
    
    /**
     * 1) **publics: num of public methods and attributes.
     * L'idea è che una classe con tanti metodi e attributi pubblici comunica molto con il resto del sistema,
     * e dunque offre più superficie per la comparsa di malfunzionamenti durante interazione.
     * Inoltre, una classe accessibile da più punti è meno mantenibile rispetto a una classe
     * accessibile solo in pochi, conosciuti modi.
     */
    private int publics;

    /**
     * 2) **NSmells**: Un alto numero di smells indica una difficile manutenzione 
     */
    private int nSmells;

    /**
     * 3) **Age**: Più una classe è vecchia e meno è probabile che
     * vengano scovati nuovi bug
     * 
     */
    private int age;            // in revisions

    /**
     * 4) **NAuth**: Più persone lavorano a una classe e più è probabile
     * che sorgano errori dovuti a equivoci, incomprensioni, cambi di stile, ...
     */
    private int nAuth;        

    /**
     * 5) **Size**: Più è grande, più è probabile che contenga bugs
     */
    private int size;           // in LOCs

    /**
     * 6) **NR**: in combinazione con age, se una classe è stata toccata poco
     * in un periodo lungo di tempo, è probabile che sia esente da bugs
     */
    private int nr;             // num of revisions

    /**
     * 7) **NFix**: se classe è fixata in continuazione
     * è probabile che sia molto prona a errori
     * 
     */
    private int nFix;

    /**
     * 8) **LOCs_touched**: Più percentuale di classe è stata modificata, più è probabile che
     * vengano introdotti dei bugs a seguito delle modifiche
     */
    private int locsTouched;

    /**
     * 9) **imported_classes**: Una classe che dipende da tante altre classi
     * è più probabile che sia soggetta a regressioni di altre
     */
    private int importedClasses;

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public int getPublics() {
        return publics;
    }

    public void setPublics(int publics) {
        this.publics = publics;
    }

    public int getnSmells() {
        return nSmells;
    }

    public void setnSmells(int nSmells) {
        this.nSmells = nSmells;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getnAuth() {
        return nAuth;
    }

    public void setnAuth(int nAuth) {
        this.nAuth = nAuth;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNr() {
        return nr;
    }

    public void setNr(int nr) {
        this.nr = nr;
    }

    public int getnFix() {
        return nFix;
    }

    public void setnFix(int nFix) {
        this.nFix = nFix;
    }

    public int getLocsTouched() {
        return locsTouched;
    }

    public void setLocsTouched(int locsTouched) {
        this.locsTouched = locsTouched;
    }

    public int getImportedClasses() {
        return importedClasses;
    }

    public void setImportedClasses(int importedClasses) {
        this.importedClasses = importedClasses;
    }

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release = release;
    }

    
}
