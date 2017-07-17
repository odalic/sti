package uk.ac.shef.dcs.kbproxy.sparql.pp.helpers;

/**
 * Created by tomasknap on 09/02/17.
 */
public class RelationDesc extends ResourceDesc {

    private String domain;
    private String range;

    public RelationDesc(String predUrl, String label, String domain, String range) {
        super(predUrl, label);
        this.domain = domain;
        this.range = range;
    }

    public String getDomain() {
        return domain;
    }

    public String getRange() {
        return range;
    }
}
