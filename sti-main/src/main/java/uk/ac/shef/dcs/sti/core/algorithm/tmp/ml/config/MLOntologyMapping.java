package uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config;

import java.util.Map;

public class MLOntologyMapping {

    Map<String, String> ontologyClassMapping;
    Map<String, String> ontologyPredicateMapping;

    public MLOntologyMapping(Map<String, String> ontologyClassMapping, Map<String, String> ontologyPredicateMapping) {
        this.ontologyClassMapping = ontologyClassMapping;
        this.ontologyPredicateMapping = ontologyPredicateMapping;
    }

    /**
     * Returns the URI of ontology class represented by given ML class label.
     * If no such mapping exists, null is returned.
     * @param mlClassLabel
     * @return
     */
    public String getOntologyClassMappingValue(String mlClassLabel) {
        return this.ontologyClassMapping.get(mlClassLabel);
    }

    /**
     * Returns the URI of ontology property (predicate) represented by given ML class label.
     * If no such mapping exists, null is returned.
     * @param mlClassLabel
     * @return
     */
    public String getOntologyPredicateMappingValue(String mlClassLabel) {
        return this.ontologyPredicateMapping.get(mlClassLabel);
    }

    /**
     * For given Class URI, returns ML class, which represents the URI.
     * @param classUri
     * @return ML class, null if no ML class represents given URI.
     */
    public String getMlClassForClassUri(String classUri) {
        for (Map.Entry<String, String> entry : this.ontologyClassMapping.entrySet()) {
            String entryClassUri = entry.getValue();
            if (entryClassUri.equals(classUri)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
