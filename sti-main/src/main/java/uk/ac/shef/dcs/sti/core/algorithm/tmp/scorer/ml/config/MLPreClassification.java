package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.config;

import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.MLPredicate;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;

import java.util.HashMap;
import java.util.Map;

public class MLPreClassification {

    private Map<Integer, TColumnHeaderAnnotation> classHeaderAnnotations;
    private Map<Integer, MLPredicate> predicates;

    public MLPreClassification() {
        this.classHeaderAnnotations = new HashMap<>();
        this.predicates = new HashMap<>();
    }

    /**
     * Adds a given header annotation to the collection of Header Annotations.
     * If Header annotation for given column already exists, it is overwritten by the
     * new annotation.
     * @param colIndex
     * @param headerAnnotation
     */
    public void addClassHeaderAnnotation(int colIndex, TColumnHeaderAnnotation headerAnnotation) {
        this.classHeaderAnnotations.put(colIndex, headerAnnotation);
    }

    /**
     * Adds a given predicate URI to the collection of Predicate URIs.
     * If predicate URI for given column already exists, it is overwritten by the
     * new annotation.
     * @param colIndex
     * @param mlPredicate
     */
    public void addPredicate(int colIndex, MLPredicate mlPredicate) {
        this.predicates.put(colIndex, mlPredicate);
    }

    public TColumnHeaderAnnotation getHeaderAnnotation(int colIndex) {
        return this.classHeaderAnnotations.get(colIndex);
    }

    public MLPredicate getPredicateAnnotation(int colIndex) {
        return this.predicates.get(colIndex);
    }

    public Map<Integer, TColumnHeaderAnnotation> getClassHeaderAnnotations() {
        return classHeaderAnnotations;
    }

    public Map<Integer, MLPredicate> getPredicates() {
        return predicates;
    }
}
