package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.config;

import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.MLPredicate;
import uk.ac.shef.dcs.sti.core.extension.annotations.Entity;
import uk.ac.shef.dcs.sti.core.extension.annotations.EntityCandidate;
import uk.ac.shef.dcs.sti.core.extension.annotations.HeaderAnnotation;
import uk.ac.shef.dcs.sti.core.extension.annotations.Score;
import uk.ac.shef.dcs.sti.core.extension.constraints.Classification;
import uk.ac.shef.dcs.sti.core.extension.positions.ColumnPosition;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    public Set<Classification> getColumnClassifications() {
        Set<Classification> classifications = new HashSet<>();
        for (Map.Entry<Integer, TColumnHeaderAnnotation> entry : this.classHeaderAnnotations.entrySet()) {
            ColumnPosition colPos = new ColumnPosition(entry.getKey());

            Clazz clazz = entry.getValue().getAnnotation();
            // there will be only 1 candidate (chosen ML class), which will also be 'chosen' for the annotation
            Set<EntityCandidate> candidates = new HashSet<>();
            candidates.add(new EntityCandidate(
                    new Entity(clazz.getId(), clazz.getLabel()),
                    new Score(entry.getValue().getFinalScore())
            ));

            HeaderAnnotation annotation = new HeaderAnnotation(candidates, candidates);
            classifications.add(new Classification(colPos, annotation));
        }
        return classifications;
    }

    /**
     * Retrieve MLPreclassification-provided set of subject column candidates.
     * The column is considered as subject column candidate, if it was classified as an Ontology class, and
     * there exist a Property column in the table, whose property contains given class column in its domain.
     * @return
     */
    public Set<ColumnPosition> getSubjectColumnPositions() {

        Set<ColumnPosition> subjectColumnPositions = new HashSet<>();

        for (Map.Entry<Integer, TColumnHeaderAnnotation> classEntry: getClassHeaderAnnotations().entrySet()) {
            String classUri = classEntry.getValue().getAnnotation().getId();
            Integer columnIndex = classEntry.getKey();

            for (Map.Entry<Integer, MLPredicate> predicateEntry: getPredicates().entrySet()) {
                if (predicateEntry.getValue().domainContains(classUri)) {
                    // there is a predicate with the class in its domain, add it to subject columns
                    subjectColumnPositions.add(new ColumnPosition(columnIndex));
                }
            }
        }

        return subjectColumnPositions;
    }
}
