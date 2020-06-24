package uk.ac.shef.dcs.kbproxy.model;

import java.net.URI;
import java.util.Collection;

public class Concept {

    private final URI uri;
    private final String label;
    private final Collection<String> alternativeLabels;
    private final Collection<String> classes;

    public Concept(final URI uri, final String label, final Collection<String> alternativeLabels,
                   final Collection<String> classes) {

        this.uri = uri;
        this.label = label;
        this.alternativeLabels = alternativeLabels;
        this.classes = classes;
    }

    public URI getUri() {
        return uri;
    }

    public String getLabel() {
        return label;
    }

    public Collection<String> getAlternativeLabels() {
        return alternativeLabels;
    }

    public Collection<String> getClasses() {
        return classes;
    }
}
