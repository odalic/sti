package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml;

import java.util.HashSet;
import java.util.Set;

public class MLPredicate {

    private final String uri;
    /**
     * URIs of classes that are defined as a domain
     * for this predicate.
     */
    private final Set<String> domainUris;

    public MLPredicate(String uri, Set<String> domainUris) {
        this.uri = uri;
        this.domainUris = domainUris;
    }

    public String getUri() {
        return uri;
    }

    public boolean domainContains(String classUri) {
        if (!this.domainUris.isEmpty()) {
            return domainUris.contains(classUri);
        } else {
            // if domain is empty, anything can be assigned to the property
            return true;
        }
    }
}
