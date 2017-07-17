package uk.ac.shef.dcs.kbproxy.sparql.pp.helpers;

/**
 * Helper class to hold description of a class
 */
public class ClassDesc {

    private String classUrl;
    private String label;

    public ClassDesc(String classUrl, String label) {
        this.label = label;
        this.classUrl = classUrl;
    }

    public String getClassUrl() {
        return classUrl;
    }

    public void setClassUrl(String classUrl) {
        this.classUrl = classUrl;
    }

    public String getLabel() {
        return label;
    }
}
