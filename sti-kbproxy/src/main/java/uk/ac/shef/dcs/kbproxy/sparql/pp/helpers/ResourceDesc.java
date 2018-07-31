package uk.ac.shef.dcs.kbproxy.sparql.pp.helpers;

/**
 * Helper class to hold description of a concept (instance data)
 *
 */
public class ResourceDesc {

    private String url;
    private String label;

    public ResourceDesc(String label) {
        this.url = "";
        this.label = label;
    }

    public ResourceDesc(String url, String label) {
        this(label);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLabel() {
        return label;
    }
}
