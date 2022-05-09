package it.torkin.dao.sonar;

enum SonarQuery{

    /**
     * Retrieves all issues related to smells in the project injected before createdBefore date (inclusive).
     * Note that if an issue is retrieved, it means that this issue was never closed, hence the smell is still present in main branch and affects all releases after createdBefore date.
     */
    GET_PROJECT_ISSUES("https://sonarcloud.io/api/issues/search?organization=%s&componentKeys=%s%s&languages=java&types=%s&ps=500&createdBefore=%s")
    ;

    private String template;
    
    private SonarQuery(String template){
        this.template = template;
    }

    @Override
    public String toString(){
        return this.template;
    }
}