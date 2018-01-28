package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.MLAttributeClassification;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.MLClassifier;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.MLException;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.RelationColumns;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TCellCellRelationAnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.scorer.AttributeValueMatcher;
import uk.ac.shef.dcs.sti.core.scorer.RelationScorer;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TMLColumnColumnRelationEnumerator extends TColumnColumnRelationEnumerator {

    private MLClassifier mlClassifier;

    public TMLColumnColumnRelationEnumerator(final AttributeValueMatcher attributeValueMatcher,
                                           final RelationScorer scorer,
                                             final MLClassifier mlClassifier) {
        super(attributeValueMatcher, scorer);
        this.mlClassifier = mlClassifier;
    }

    private void mlOnlyRelationDiscovery(final TAnnotation annotations, final Table table,
                                         final int subjectCol, final Constraints constraints) throws MLException {

        final Map<Integer, DataTypeClassifier.DataType> columnDataTypes = getDataTypesOfColumns(table);

        // for each row
        for (int row = 0; row < table.getNumRows(); row++) {

            // return list of cell values (in row), that needs to be matched
            // respect suggested relations passed by user in constrains
            final Map<Integer, String> cellValuesToMatch = getCellValuesToMatch(table, subjectCol, row, columnDataTypes, constraints);

            // a map of cell values, that needs to be matched by 'legacy' relation enumeration methods,
            // as the ML classifier was unable to classify them
            final Map<Integer, String> cellValuesToMatchLegacy = new HashMap<>();

            // determine relation between subjectCol and other columns using ML classifier, together with a score
            final Map<Integer, MLAttributeClassification> mlClassificationScores = new HashMap<>();
            for (Map.Entry<Integer, String> cellValueToMatch : cellValuesToMatch.entrySet()) {
                Integer columnIndex = cellValueToMatch.getKey();
                String cellValue = cellValueToMatch.getValue();

                MLAttributeClassification mlClassification = mlClassifier.classifyToAttribute(cellValue);
                if (mlClassification.nonEmpty()) {
                    mlClassificationScores.put(columnIndex, mlClassification);
                } else {
                    cellValuesToMatchLegacy.put(columnIndex, cellValue);
                }
            }

            // perform matching and scoring
            // key=col id; value: contains the attr that matched with the highest score against cell in
            // that column
            for (final Map.Entry<Integer, MLAttributeClassification> e : mlClassificationScores.entrySet()) {
                final RelationColumns subCol_to_objCol = new RelationColumns(subjectCol, e.getKey());

                final Attribute mlClassificationAttribute = e.getValue().getAttribute();

                final String relationURI = mlClassificationAttribute.getRelationURI();
                final String relationLabel = mlClassificationAttribute.getRelationLabel();
                final List<Attribute> matchedValues = new ArrayList<>();
                matchedValues.add(mlClassificationAttribute);

                final TCellCellRelationAnotation cellcellRelation = new TCellCellRelationAnotation(
                        subCol_to_objCol, row, relationURI, relationLabel, matchedValues, e.getValue().getScore()
                );
                annotations.addCellCellRelation(cellcellRelation);
            }

            // annotate missing values using legacy Odalic (TMP) method
            if (!cellValuesToMatchLegacy.isEmpty()) {
                try {
                    Map<Integer, List<Pair<Attribute, Double>>> cellMatchScores =
                            computeCellMatchScoresForRow(annotations, row, subjectCol, columnDataTypes, cellValuesToMatchLegacy);

                    addCellCellRelationAnnotationsFromCellMatchScores(annotations, cellMatchScores, row, subjectCol);
                }catch (STIException e) {
                    throw new MLException("Legacy Relation Discovery failed: " + e.getMessage(), e);
                }

            }
        }
    }

    @Override
    public int runRelationEnumeration(final TAnnotation annotations, final Table table,
                                      final int subjectCol, final Constraints constraints) throws STIException {
        resetSuggestedRelationPositionsVisited();
        // discover relations using ML classifier
        try {
            // TODO ml relation discovery, and if fails, legacy odalic discovery

            mlOnlyRelationDiscovery(annotations, table, subjectCol, constraints);
        } catch (MLException e) {
            throw new STIException("ML classifier error: " + e.getMessage(), e);
        }
        // now we have created relation annotations per row, consolidate them to create column-column
        // relation
        enumerateColumnColumnRelation(annotations, table, constraints);
        return annotations.getCellcellRelations().size() + getSuggestedRelationPositionsVisited();
    }

}
