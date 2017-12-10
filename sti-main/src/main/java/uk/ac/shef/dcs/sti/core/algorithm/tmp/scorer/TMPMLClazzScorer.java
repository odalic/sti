package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.MLClassifier;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.scorer.ClazzScorer;
import uk.ac.shef.dcs.util.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Extension of TMPClazzScorer, which is capable of using Machine Learning
 * algorithm to suggest Column Clazz.
 */
public class TMPMLClazzScorer implements ClazzScorer {

    private static final Logger LOG = LoggerFactory.getLogger(TMPClazzScorer.class.getName());

    private ClazzScorer clazzScorer;
    private MLClassifier mlClassifier;

    public TMPMLClazzScorer(ClazzScorer tmpClazzScorer, MLClassifier mlClassifier) {
        this.clazzScorer = tmpClazzScorer;
        this.mlClassifier = mlClassifier;
    }


    // TODO implement
    @Override
    public List<TColumnHeaderAnnotation> computeCCScore(Collection<TColumnHeaderAnnotation> candidates, Table table, int column) throws STIException {
        return clazzScorer.computeCCScore(candidates, table, column);
    }

    @Override
    public List<TColumnHeaderAnnotation> computeCEScore(List<Pair<Entity, Map<String, Double>>> entities,
                                                        Collection<TColumnHeaderAnnotation> existingHeaderAnnotations,
                                                        Table table, int row, int column) throws STIException {

        return clazzScorer.computeCEScore(entities, existingHeaderAnnotations, table, row, column);
    }

    @Override
    public double computeDC(TColumnHeaderAnnotation ha, List<String> domain_representation) throws STIException {
        return clazzScorer.computeDC(ha, domain_representation);
    }

    @Override
    public List<TColumnHeaderAnnotation> computeElementScores(List<Pair<Entity, Map<String, Double>>> input,
                                                              Collection<TColumnHeaderAnnotation> headerAnnotationCandidates,
                                                              Table table, List<Integer> rows, int column) throws STIException {

        return computeElementScores(input, headerAnnotationCandidates, table, rows, column);
    }

    @Override
    public Map<String, Double> computeFinal(TColumnHeaderAnnotation ha, int tableRowsTotal) {
        return clazzScorer.computeFinal(ha, tableRowsTotal);
    }
}
