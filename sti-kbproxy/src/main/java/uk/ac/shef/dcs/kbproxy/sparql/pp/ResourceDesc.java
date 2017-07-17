package uk.ac.shef.dcs.kbproxy.sparql.pp;

/**
 * Helper class to hold description of a class
 *
 * //TODO have two helpers - one for holding URIs of concepts (instances), second to hold classes
 *
 */
public class ResourceDesc {

    private String classUrl;
    private String conceptUrl;
    private String label;

    public ResourceDesc(String label) {
        this.conceptUrl = "";
        this.label = label;
    }

    public ResourceDesc(String classUrl, String label) {
        this(label);
        this.classUrl = classUrl;
    }

    public String getClassUrl() {
        return classUrl;
    }

    public void setClassUrl(String classUrl) {
        this.classUrl = classUrl;
    }

    public String getConceptUrl() {
        return conceptUrl;
    }

    public void setConceptUrl(String conceptUrl) {
        this.conceptUrl = conceptUrl;
    }

    public String getLabel() {
        return label;
    }
}
