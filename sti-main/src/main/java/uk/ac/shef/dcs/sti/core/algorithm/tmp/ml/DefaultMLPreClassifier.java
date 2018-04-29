package uk.ac.shef.dcs.sti.core.algorithm.tmp.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.classifier.MLClassification;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.classifier.MLClassifier;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.exception.MLException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.MLOntologyDefinition;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.MLOntologyMapping;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.MLPreClassification;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.exception.MLOntologyClassNotFoundException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.exception.MLOntologyPropertyNotFoundException;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultMLPreClassifier implements MLPreClassifier {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultMLPreClassifier.class);

    private MLClassifier mlClassifier;
    private MLOntologyMapping mlOntologyMapping;
    private MLOntologyDefinition mlOntologyDefinition;

    public DefaultMLPreClassifier(MLClassifier mlClassifier, MLOntologyMapping mlOntologyMapping,
                                  MLOntologyDefinition mlOntologyDefinition) {
        this.mlClassifier = mlClassifier;
        this.mlOntologyMapping = mlOntologyMapping;
        this.mlOntologyDefinition = mlOntologyDefinition;
    }

    public MLPreClassification preClassificate(Table table, Set<Integer> ignoreColumns) throws MLException {
        // for each column, classify every cell and select the ML class with best score
        Map<Integer, MLClassificationWithScore> columnClassifications = new HashMap<>();

        for (int col = 0; col < table.getNumCols(); col++) {
            if (ignoreColumns.contains(col)) {
                continue;
            }

            // get column ML class candidates from each row
            Map<String, Integer> columnScores = getMLClassCandidatesForColumn(table, col);
            // select winning ML Class for column
            MLClassificationWithScore winningMlClassForCol = selectWinningMlClassForColumn(columnScores);
            if (winningMlClassForCol != null) {
                columnClassifications.put(col, winningMlClassForCol);
            }
        }
        // translate ML class to actual ontology class/predicate & assemble result for all columns
        MLPreClassification mlPreClassification = resolveOntologyUrisAndAssembleResult(table, columnClassifications);
        return mlPreClassification;
    }

    private Map<String, Integer> getMLClassCandidatesForColumn(Table table, int col) {
        Map<String, Integer> columnScores = new HashMap<>();

        for (int row = 0; row < table.getNumRows(); row++) {
            String content = table.getContentCell(row, col).getText();

            // classify and update scores
            try {
                MLClassification mlClass = mlClassifier.classify(content);
                if (mlClass != null) {
                    addClassOrUpdateClassScore(mlClass, columnScores);
                }
            } catch (MLException e) {
                LOG.warn("ML Classification failed for '" + content + "'!");
            }
        }
        return columnScores;
    }

    private void addClassOrUpdateClassScore(MLClassification mlClass, Map<String, Integer> scoreMap) {
        scoreMap.merge(mlClass.getMlClass(),1, (oldVal, defValue) -> oldVal + defValue);
    }

    private MLClassificationWithScore selectWinningMlClassForColumn(Map<String, Integer> scoreMap) {
        MLClassificationWithScore winningMlClass = null;
        Integer winningMlClassScore = null;
        for (Map.Entry<String, Integer> entry : scoreMap.entrySet()) {
            if (winningMlClassScore == null || winningMlClassScore < entry.getValue()) {
                winningMlClass = new MLClassificationWithScore(entry.getKey(), entry.getValue());
                winningMlClassScore = entry.getValue();
            }
        }
        return winningMlClass;
    }

    private MLPreClassification resolveOntologyUrisAndAssembleResult(Table table,
            Map<Integer, MLClassificationWithScore> columnClassifications) throws MLException {

        MLPreClassification mlPreClassification = new MLPreClassification();

        for (Map.Entry<Integer, MLClassificationWithScore> colEntry: columnClassifications.entrySet()) {
            // if a ML classification is found, resolve URI, determine if class or predicate
            String classUri = mlOntologyMapping.getOntologyClassMappingValue(colEntry.getValue().getMlClass());
            if (classUri != null) {
                final TColumnHeaderAnnotation headerAnnotation = new TColumnHeaderAnnotation(
                        table.getColumnHeader(colEntry.getKey()).getHeaderText(),
                        loadClazz(classUri),
                        new Double(colEntry.getValue().getScore())
                );
                mlPreClassification.addClassHeaderAnnotation(colEntry.getKey(), headerAnnotation);

            } else {
                final String predicateUri = mlOntologyMapping.getOntologyPredicateMappingValue(colEntry.getValue().getMlClass());
                if (predicateUri != null) {
                    // fetch domain of given predicate from ontology definition
                    Set<String> domainUris = loadDomainOfPredicate(predicateUri);
                    mlPreClassification.addPredicate(colEntry.getKey(), new MLPredicate(predicateUri, domainUris));
                } else {
                    LOG.error("No OntologyMapping matches ML Class '" + colEntry.getValue().getMlClass() +
                            "' (Column: " + colEntry.getKey() + ")! Ignoring column.");
                }
            }
        }
        return mlPreClassification;
    }

    /**
     * Load domain of given predicate from the Ontology Definition.
     * @param predicateUri
     * @return
     */
    private Set<String> loadDomainOfPredicate(String predicateUri) throws MLException {
        try {
            return this.mlOntologyDefinition.findDomainClassIRIsOfProperty(predicateUri);
        } catch (MLOntologyPropertyNotFoundException e) {
            throw new MLException(e.getMessage(), e);
        }
    }

    private Clazz loadClazz(String clazzUri) throws MLException {
        try {
            return this.mlOntologyDefinition.loadClazz(clazzUri);
        } catch (MLOntologyClassNotFoundException e) {
            throw new MLException(e.getMessage(), e);
        }
    }

    public MLOntologyDefinition getMlOntologyDefinition() {
        return mlOntologyDefinition;
    }
}

class MLClassificationWithScore {
    private String mlClass;
    private Integer score;

    public MLClassificationWithScore(String mlClass, Integer score) {
        this.mlClass = mlClass;
        this.score = score;
    }

    public String getMlClass() {
        return mlClass;
    }

    public Integer getScore() {
        return score;
    }
}
