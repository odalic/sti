package uk.ac.shef.dcs.kbproxy.sparql.pp.helpers;

import uk.ac.shef.dcs.kbproxy.model.PropertyType;

/**
 * Created by tomasknap on 09/02/17.
 */
public class RelationDesc extends ResourceDesc {

    private String domain;
    private String range;
    private PropertyType type;

    public PropertyType getType() {
        return type;
    }

    public RelationDesc(String predUrl, String label, String domain, String range, PropertyType type) {
        super(predUrl, label);
        this.domain = domain;
        this.range = range;
        this.type = type;

    }

    public RelationDesc(String predUrl, String label, String domain, PropertyType type) {
        super(predUrl, label);
        this.domain = domain;
        this.type = type;
    }

    public String getDomain() {
        return domain;
    }

    public String getRange() {
        return range;
    }
}
