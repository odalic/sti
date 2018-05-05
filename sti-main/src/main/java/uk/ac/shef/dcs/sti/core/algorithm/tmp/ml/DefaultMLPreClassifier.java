package uk.ac.shef.dcs.sti.core.algorithm.tmp.ml;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.classifier.MLClassification;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.classifier.MLClassificationWithScore;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.classifier.MLClassifier;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.exception.MLException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.MLOntologyDefinition;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.MLOntologyMapping;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.MLPreClassification;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.exception.MLOntologyClassNotFoundException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.exception.MLOntologyPropertyNotFoundException;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.*;
import java.util.stream.Collectors;

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
        List<ColumnWithSortedClasses> colsWithSortedClasses = new ArrayList<>();

        for (int col = 0; col < table.getNumCols(); col++) {
            if (ignoreColumns.contains(col)) {
                continue;
            }

            // get column ML class candidates from each row
            Map<String, MlClassWithScoreOccurences> columnScores = getMLClassCandidatesForColumn(table, col);
            ColumnWithSortedClasses colWithSortedClasses = sortColumnClasses(col, columnScores);
            colsWithSortedClasses.add(colWithSortedClasses);
        }

        // pick each class just once, for column where it reached highest score
        columnClassifications = getColumnClassifications(colsWithSortedClasses);


        // translate ML class to actual ontology class/predicate & assemble result for all columns
        MLPreClassification mlPreClassification = resolveOntologyUrisAndAssembleResult(table, columnClassifications);
        return mlPreClassification;
    }

    private Map<String, MlClassWithScoreOccurences> getMLClassCandidatesForColumn(Table table, int col) {
        Map<String, MlClassWithScoreOccurences> columnScores = new HashMap<>();

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

    private ColumnWithSortedClasses sortColumnClasses(int column, Map<String, MlClassWithScoreOccurences> scoreMap) {
        Map<Double, List<String>> sortedMap = new TreeMap<>();

        scoreMap
            .entrySet()
            .stream()
            .forEach(cwo -> {
                //Double score = cwo.getValue().getWeightedAverage();
                Double score = (double) cwo.getValue().getOccurences();
                sortedMap.computeIfAbsent(score, k -> new ArrayList<>()).add(cwo.getKey());
            });

        List<MLClassificationWithScore> sortedList = new ArrayList<>();
        for (Map.Entry<Double, List<String>> scoreEntry : sortedMap.entrySet()) {
            for (String mlClass : scoreEntry.getValue()) {
                sortedList.add(new MLClassificationWithScore(mlClass, scoreEntry.getKey()));
            }
        }

        return new ColumnWithSortedClasses(column, sortedList);
    }

    private void addClassOrUpdateClassScore(MLClassification mlClass, Map<String, MlClassWithScoreOccurences> scoreMap) {
        MLClassificationWithScore highestScoreValue = mlClass.getHighestScoreMlClass(this.mlClassifier.getConfidenceThreshold());
        if (highestScoreValue != null) {

            MlClassWithScoreOccurences mlClassScore = scoreMap.get(highestScoreValue.getMlClass());
            if (mlClassScore == null) {
                mlClassScore = new MlClassWithScoreOccurences(highestScoreValue.getMlClass());
                scoreMap.put(highestScoreValue.getMlClass(), mlClassScore);
            }
            mlClassScore.addOccurence(highestScoreValue.getScore());
        }
    }

    private Map<Integer, MLClassificationWithScore> getColumnClassifications(List<ColumnWithSortedClasses> columns) {

        // take winning class for each column
        // if the class is suitable only for 1 column, assign it to that column
        // if the class is suitable for more columns, assign it to the one, where the class has highest score
        // repeat for columns with no class assigned (which have a class available), (dont include classes that were
        // already assigned)
        Map<Integer, MLClassificationWithScore> result = new HashMap<>();

        List<ColumnWithSortedClasses> columnsToProcess = columns;
        Set<String> alreadyAssignedClasses = new HashSet<>();
        Set<Integer> columnsToIgnore = new HashSet<>();
        int loopNo = 0;
        while (!columnsToProcess.isEmpty()) {
            LOG.debug("ML PreClassification - getColumnClassifications - loop " + loopNo);

            // build a map of classes with (column, score)
            Map<String, List<ColumnWithScore>> classesScoreMap = new HashMap<>();

            for (ColumnWithSortedClasses column: columnsToProcess) {
                // get current top class of the column
                MLClassificationWithScore topColumnClass = column.getClassAtCurrentPointer();

                if (topColumnClass != null) {
                    // if class is already assigned, skip the column it for this round
                    if (!alreadyAssignedClasses.contains(topColumnClass.getMlClass())) {
                        classesScoreMap.computeIfAbsent(topColumnClass.getMlClass(), k -> new ArrayList<>())
                                .add(new ColumnWithScore(column.getColumn(), topColumnClass.getScore()));
                    }

                    // and move the pointe to the top class so it points to the next one in order
                    // so its ready for next run, if necessary
                    column.movePointerToNextClass();
                } else {
                    // ignore column
                    columnsToIgnore.add(column.getColumn());
                }
            }

            List<ColumnWithSortedClasses> newColumnsToProcess = new ArrayList<>();

            // assign each class to the column with highest score
            for (Map.Entry<String, List<ColumnWithScore>> classEntry : classesScoreMap.entrySet()) {
                // multiple classes for same col - should not occur, as only 1 class is taken from each column

                String mlClass = classEntry.getKey();
                List<ColumnWithScore> candidateColumns = classEntry.getValue();
                Set<Integer> candidateColumnIds = candidateColumns
                        .stream()
                        .map(ColumnWithScore::getColumn)
                        .collect(Collectors.toSet());

                ColumnWithScore winningColumnForClass = selectWinningColumnForClass(candidateColumns);
                int winningColumn = winningColumnForClass.getColumn();
                // Add to result
                alreadyAssignedClasses.add(mlClass);
                result.put(winningColumn, new MLClassificationWithScore(mlClass, winningColumnForClass.getScore()));

                // mark non-chosen columns to be run again
                newColumnsToProcess.addAll(
                    columnsToProcess
                        .stream()
                        .filter(c -> candidateColumnIds.contains(c.getColumn()) && c.getColumn() != winningColumn)
                        .collect(Collectors.toList())
                );
            }

            // perform another round with unclassified columns (which are not ignored)
            columnsToProcess = newColumnsToProcess
                    .stream()
                    .filter(c -> !columnsToIgnore.contains(c.getColumn()))
                    .collect(Collectors.toList());
            loopNo += 1;
        }
        return result;
    }

    private ColumnWithScore selectWinningColumnForClass(List<ColumnWithScore> scoredColumns) {
        // input should never be empty on correct usage
        Preconditions.checkArgument(!scoredColumns.isEmpty());

        // multiple cols with same score - choose one by a policy
        SortedMap<Double, List<ColumnWithScore>> scoreMap = new TreeMap<>(Collections.reverseOrder());
        // populate sorted map
        scoredColumns.stream().forEach(c ->
            scoreMap.computeIfAbsent(c.getScore(), k -> new ArrayList<>()).add(c)
        );
        return selectFirstColumnFromSameScoreColumns(scoreMap.entrySet().iterator().next().getValue());
    }

    private ColumnWithScore selectFirstColumnFromSameScoreColumns(List<ColumnWithScore> columnsWithSameScore) {
        // input should never be empty on correct usage
        Preconditions.checkArgument(!columnsWithSameScore.isEmpty());
        return columnsWithSameScore.get(0);
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

class MlClassWithScoreOccurences {
    private String mlClass;
    private Map<Double, Integer> scoreOccurences;
    private Double weightedAverage = null;
    private Integer occurences = null;

    public MlClassWithScoreOccurences(String mlClass) {
        this.mlClass = mlClass;
        this.scoreOccurences = new HashMap<>();
    }

    public void addOccurence(Double score) {
        scoreOccurences.merge(score, 1, (oldVal, defValue) -> oldVal + defValue);
    }

    public double getWeightedAverage() {
        if (this.weightedAverage == null) {
            this.computeWeightedAverage();
        }
        return this.weightedAverage;
    }

    public int getOccurences() {
        if (this.occurences == null) {
            this.computeWeightedAverage();
        }
        return this.occurences;
    }

    private void computeWeightedAverage() {
        double weightedSum = 0d;
        int totalOccurences = 0;

        for (Map.Entry<Double, Integer> scoreOccurence: scoreOccurences.entrySet()) {
            weightedSum += scoreOccurence.getKey() * scoreOccurence.getValue();
            totalOccurences += scoreOccurence.getValue();
        }

        if (totalOccurences > 0) {
            this.weightedAverage = weightedSum / totalOccurences;
        } else {
            this.weightedAverage = 0d;
        }
        this.occurences = totalOccurences;
    }
}

class ColumnWithSortedClasses {

    private int column;
    private List<MLClassificationWithScore> classesWithScore;

    private int classPositionPointer;

    public ColumnWithSortedClasses(int column, List<MLClassificationWithScore> classesWithScore) {
        this.column = column;
        this.classesWithScore = classesWithScore;
        this.classPositionPointer = this.classesWithScore.size() -1;
    }

    public int getColumn() {
        return column;
    }

    public MLClassificationWithScore getClassAtCurrentPointer() {
        if (this.classPositionPointer > -1) {
            return this.classesWithScore.get(this.classPositionPointer);
        } else {
            return null;
        }
    }

    public void movePointerToNextClass() {
        this.classPositionPointer -= 1;
    }
}

class ColumnWithScore {
    private int column;
    private Double score;

    public ColumnWithScore(int column, Double score) {
        this.column = column;
        this.score = score;
    }

    public int getColumn() {
        return column;
    }

    public Double getScore() {
        return score;
    }
}