package it.torkin.miners;

public enum Feature {

    CODE_SMELLS("codeSmells")
    ;

    private String name;
    
    private Feature(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    

}
