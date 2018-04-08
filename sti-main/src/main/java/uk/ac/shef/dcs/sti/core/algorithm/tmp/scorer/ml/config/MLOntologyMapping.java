package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.config;

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
}
