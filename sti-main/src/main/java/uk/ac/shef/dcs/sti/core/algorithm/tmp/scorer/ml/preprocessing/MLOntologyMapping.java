package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.preprocessing;

import java.util.HashMap;
import java.util.Map;

public class MLOntologyMapping {

    Map<String, String> ontologyMapping = new HashMap<>();

    public MLOntologyMapping(Map<String, String> ontologyMapping) {
        this.ontologyMapping = ontologyMapping;
    }

    /**
     * Returns the URI of ontology class/property represented by given ML class label.
     * If no such mapping exists, null is returned.
     * @param mlClassLabel
     * @return
     */
    public String getOntologyMappingValue(String mlClassLabel) {
        return this.ontologyMapping.get(mlClassLabel);
    }
}
