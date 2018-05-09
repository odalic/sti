package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import cz.cuni.mff.xrg.odalic.util.parsing.UriParsingUtil;
import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.sparql.SparqlAttribute;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.MLPredicate;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.MLOntologyDefinition;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.MLPreClassification;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.UriWithScore;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.exception.MLException;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.*;
import uk.ac.shef.dcs.sti.core.scorer.AttributeValueMatcher;
import uk.ac.shef.dcs.sti.core.scorer.RelationScorer;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.util.Pair;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TMLColumnColumnRelationEnumerator extends TColumnColumnRelationEnumerator {

    private final MLOntologyDefinition mlOntologyDefinition;

    public TMLColumnColumnRelationEnumerator(final AttributeValueMatcher attributeValueMatcher,
                                           final RelationScorer scorer,
                                             final MLOntologyDefinition mlOntologyDefinition) {
        super(attributeValueMatcher, scorer);
        this.mlOntologyDefinition = mlOntologyDefinition;
    }


    private void mlAndLegacyRelationDiscovery(final TAnnotation annotations, final Table table,
                                         final int subjectCol, final MLPreClassification mlPreClassification,
                                         final Constraints constraints) throws MLException {

        final Map<Integer, DataTypeClassifier.DataType> columnDataTypes = getDataTypesOfColumns(table);

        final Set<Integer> columnsToMatchML = getColumnsToMatch(subjectCol, columnDataTypes, constraints);

        // Use ML data from MLPreClassification phase to suggest relations

        // since the subject columns are processed in ascending order, the highest chance of an ambiguous
        // relation to be assigned to column with lowest index (as the "main" subject column is usually one of
        // first columns in the table
        Set<Integer> mlMatchedColumns = new HashSet<>();

        if (mlPreClassification.getHeaderAnnotation(subjectCol) != null) {
            // retrieve winning header annotation of subject col (if ML is applied, there should be just 1 annotation)
            if (annotations.getHeaderAnnotation(subjectCol).length > 0) {
                TColumnHeaderAnnotation scHeaderAnnotation = annotations.getHeaderAnnotation(subjectCol)[0];

                for (int col : columnsToMatchML) {
                    // is col a class? If yes, try to find a predicate between subjecCol and Col in the ontology
                    if (annotations.getHeaderAnnotation(col).length > 0) {
                        String subjectColClassUri = scHeaderAnnotation.getAnnotation().getId();
                        String colClassUri = annotations.getHeaderAnnotation(col)[0].getAnnotation().getId();

                        List<UriWithScore> foundPropertyUris =
                                mlOntologyDefinition.findPropertyForSubjectObject(subjectColClassUri, colClassUri);

                        // filter out already assigned predicates
                        List<UriWithScore> unassignedFoundPropertyUris = foundPropertyUris
                                .stream()
                                .filter(uws -> !annotations.isAlreadyMlDiscoveredRelationPredicate(uws.getUri()))
                                .collect(Collectors.toList());

                        if (!unassignedFoundPropertyUris.isEmpty()) {
                            // create attribute
                            final TCellCellRelationAnotation cellcellRelation =
                                    createObjectPropertyCellCellRelationAnnotation(
                                            subjectCol, col, unassignedFoundPropertyUris.get(0).getUri(), colClassUri
                                    );

                            addCellCellRelation(col, cellcellRelation, annotations, mlMatchedColumns);
                        }
                    } else {
                        MLPredicate mlPredicate = mlPreClassification.getPredicateAnnotation(col);
                        // if not, verify if the domain of ML predicate is same as type of subjectCol,
                        // and whether the property has not been assigned yet to other subject column.
                        // if conditions are met, create attribute
                        if (mlPredicate != null && mlPredicate.domainContains(scHeaderAnnotation.getAnnotation().getId())
                                && !annotations.isAlreadyMlDiscoveredRelationPredicate(mlPredicate.getUri())
                            ) {
                            // create annotation
                            final TCellCellRelationAnotation cellcellRelation =
                                    createDataPropertyCellCellRelationAnnotation(subjectCol, col, mlPredicate);

                            addCellCellRelation(col, cellcellRelation, annotations, mlMatchedColumns);
                        }
                    }
                }
            }
        }

        // try to determine unmatched columns using legacy method
        // for each row
        for (int row = 0; row < table.getNumRows(); row++) {

            // return list of cell values (in row), that needs to be matched
            // respect suggested relations passed by user in constrains
            // dont include already determined relations from ML
            final Map<Integer, String> cellValuesToMatchLegacy = getCellValuesToMatchLegacy(table, subjectCol, row, columnDataTypes,
                    mlMatchedColumns, constraints);

            // annotate missing values using legacy Odalic (TMP) method
            if (!cellValuesToMatchLegacy.isEmpty()) {
                try {
                    Map<Integer, List<Pair<Attribute, Double>>> cellMatchScores =
                            computeCellMatchScoresForRow(annotations, row, subjectCol, columnDataTypes, cellValuesToMatchLegacy);

                    addCellCellRelationAnnotationsFromCellMatchScores(annotations, cellMatchScores, row, subjectCol);
                } catch (STIException e) {
                    throw new MLException("Legacy Relation Discovery failed: " + e.getMessage(), e);
                }

            }
        }
    }

    private void addCellCellRelation(final int col, final TCellCellRelationAnotation cellcellRelation,
                                     final TAnnotation annotations, final Set<Integer> mlMatchedColumns) {
        annotations.addCellCellRelation(cellcellRelation);
        annotations.addMlDiscoveredRelationPredicate(cellcellRelation.getRelationURI());
        mlMatchedColumns.add(col);
    }

    protected Map<Integer, String> getCellValuesToMatchLegacy(final Table table, int subjectCol, int row,
                                                        final Map<Integer, DataTypeClassifier.DataType> columnDataTypes,
                                                        final Set<Integer> mlMatchedColumns,
                                                        final Constraints constraints) {

        final Map<Integer, String> cellValuesToMatch = new HashMap<>();
        for (final int col : columnDataTypes.keySet()) {
            if ((col != subjectCol)
                    && !isRelationSuggested(subjectCol, col, constraints.getColumnRelations())
                    && !mlMatchedColumns.contains(col)) {
                final String cellValue = table.getContentCell(row, col).getText();
                cellValuesToMatch.put(col, cellValue);
            }
        }
        return cellValuesToMatch;
    }

    private Set<Integer> getColumnsToMatch(int subjectCol, final Map<Integer, DataTypeClassifier.DataType> columnDataTypes,
                                             final Constraints constraints) {

        final Set<Integer> colsToMatch = new HashSet<>();
        for (final int col : columnDataTypes.keySet()) {
            if ((col != subjectCol)
                    && !isRelationSuggested(subjectCol, col, constraints.getColumnRelations())) {
                colsToMatch.add(col);
            }
        }
        return colsToMatch;
    }

    private TCellCellRelationAnotation createDataPropertyCellCellRelationAnnotation(int subjectCol, int col,
                                                                                    MLPredicate mlPredicate) {
        return createPropertyCellCellRelationAnnotation(subjectCol, col, mlPredicate.getUri(), "", null);
    }

    private TCellCellRelationAnotation createObjectPropertyCellCellRelationAnnotation(int subjectCol, int col,
                                                                                    String predicateUri, String valueUri) {

        return createPropertyCellCellRelationAnnotation(subjectCol, col, predicateUri, "", valueUri);
    }

    private TCellCellRelationAnotation createPropertyCellCellRelationAnnotation(int subjectCol, int col,
                                                                                String predicateUri,
                                                                                String value, String valueUri) {
        // we are classifying column as a whole, not per row, so there is only 1 candidate
        int row = 0;
        double score = 1.0;

        final RelationColumns subCol_to_objCol = new RelationColumns(subjectCol, col);

        // create attribute
        boolean applyUriLabelHeuristics = true;
        final Attribute mlClassificationAttribute = new SparqlAttribute(
                UriParsingUtil.parseLabelFromResourceUri(predicateUri, applyUriLabelHeuristics),
                predicateUri,  value, valueUri
        );

        final String relationURI = mlClassificationAttribute.getRelationURI();
        final String relationLabel = mlClassificationAttribute.getRelationLabel();
        final List<Attribute> matchedValues = new ArrayList<>();
        matchedValues.add(mlClassificationAttribute);

        return new TCellCellRelationAnotation(
                subCol_to_objCol, row, relationURI, relationLabel, matchedValues, score
        );
    }

    @Override
    public int runRelationEnumeration(final TAnnotation annotations, final Table table,
                                      final int subjectCol, final MLPreClassification mlPreClassification,
                                      final Constraints constraints) throws STIException {
        resetSuggestedRelationPositionsVisited();
        // discover relations using ML classifier
        try {
            mlAndLegacyRelationDiscovery(annotations, table, subjectCol, mlPreClassification, constraints);
        } catch (MLException e) {
            throw new STIException("ML classifier error: " + e.getMessage(), e);
        }
        // now we have created relation annotations per row, consolidate them to create column-column
        // relation
        enumerateColumnColumnRelation(annotations, table, constraints);
        return annotations.getCellcellRelations().size() + getSuggestedRelationPositionsVisited();
    }

}
