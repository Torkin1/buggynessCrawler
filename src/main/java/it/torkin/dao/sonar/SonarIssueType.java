package it.torkin.dao.sonar;

enum SonarIssueType{

    CODE_SMELL("CODE_SMELL")
    ;

    private String key;

    private SonarIssueType(String key){
        this.key = key;
    }

    @Override
    public String toString(){
        return this.key;
    }

}