package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.extension.annotations.EntityCandidate;
import uk.ac.shef.dcs.sti.core.extension.constraints.ColumnRelation;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.RelationColumns;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TCellCellRelationAnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnColumnRelationAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.scorer.AttributeValueMatcher;
import uk.ac.shef.dcs.sti.core.scorer.RelationScorer;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.util.Pair;

/**
 */
public class TColumnColumnRelationEnumerator {

  private final AttributeValueMatcher attributeValueMatcher;
  private final RelationScorer relationScorer;

  private int suggestedRelationPositionsVisited;

  public TColumnColumnRelationEnumerator(final AttributeValueMatcher attributeValueMatcher,
      final RelationScorer scorer) {
    this.attributeValueMatcher = attributeValueMatcher;
    this.relationScorer = scorer;
  }

  private void enumerateColumnColumnRelation(final TAnnotation annotations, final Table table,
      final Constraints constraints) throws STIException {
    for (final Map.Entry<RelationColumns, Map<Integer, List<TCellCellRelationAnotation>>> entry : annotations
        .getCellcellRelations().entrySet()) {
      final RelationColumns key = entry.getKey(); // relation's direction
      // map containing row and the cellcellrelations for that row
      final Map<Integer, List<TCellCellRelationAnotation>> value = entry.getValue();

      // go through every row, update header binary relation scores
      List<TColumnColumnRelationAnnotation> columnColumnRelationAnnotations = new ArrayList<>();
      for (final Map.Entry<Integer, List<TCellCellRelationAnotation>> e : value.entrySet()) {
        columnColumnRelationAnnotations = this.relationScorer.computeElementScores(e.getValue(),
            columnColumnRelationAnnotations, key.getSubjectCol(), key.getObjectCol(), table);
      }

      // now collect element scores to create final score, also generate supporting rows
      for (final TColumnColumnRelationAnnotation relation : columnColumnRelationAnnotations) {
        this.relationScorer.computeFinal(relation, table.getNumRows());
        for (final Map.Entry<Integer, List<TCellCellRelationAnotation>> e : value.entrySet()) {
          for (final TCellCellRelationAnotation cbr : e.getValue()) {
            if (relation.getRelationURI().equals(cbr.getRelationURI())) {
              relation.addSupportingRow(e.getKey());
              break;
            }
          }
        }
      }
      final List<TColumnColumnRelationAnnotation> sorted =
          new ArrayList<>(columnColumnRelationAnnotations);
      Collections.sort(sorted);
      for (final TColumnColumnRelationAnnotation hbr : sorted) {
        annotations.addColumnColumnRelation(hbr);
      }

    }

    // (added): set relations suggested by the user
    for (final ColumnRelation relation : constraints.getColumnRelations()) {
      if (relation.getAnnotation().getChosen().isEmpty()) {
        annotations.addEmptyColumnColumnRelation(
            new RelationColumns(relation.getPosition().getFirstIndex(),
                relation.getPosition().getSecondIndex()));
      }
      for (final EntityCandidate suggestion : relation.getAnnotation().getChosen()) {
        annotations.addColumnColumnRelation(new TColumnColumnRelationAnnotation(
            new RelationColumns(relation.getPosition().getFirstIndex(),
                relation.getPosition().getSecondIndex()),
            suggestion.getEntity().getResource(), suggestion.getEntity().getLabel(),
            suggestion.getScore().getValue()));
      }
    }
  }

  /**
   * returns the number of columns that form relation with the subjectCol
   * <p>
   * when new relation created, supporting row info is also added
   */
  protected void generateCellCellRelations(final TAnnotation annotations, final Table table,
      final int subjectCol) throws STIException {
    generateCellCellRelations(annotations, table, subjectCol, new Constraints());
  }

  /**
   * returns the number of columns that form relation with the subjectCol
   * <p>
   * when new relation created, supporting row info is also added
   */
  private void generateCellCellRelations(final TAnnotation annotations, final Table table,
      final int subjectCol, final Constraints constraints) throws STIException {
    // select columns that are likely to form a relation with subject column
    final Map<Integer, DataTypeClassifier.DataType> columnDataTypes = new HashMap<>();
    for (int c = 0; c < table.getNumCols(); c++) {
      final DataTypeClassifier.DataType type = table.getColumnHeader(c).getTypes().get(0).getType();
      columnDataTypes.put(c, type);

//      if (type.equals(DataTypeClassifier.DataType.ORDERED_NUMBER)) {
//        continue; // ordered numbered columns are not interesting
//      } else {
//        columnDataTypes.put(c, type);
//      }
    }

    // for each row, get the annotation for that (row, col)
    for (int row = 0; row < table.getNumRows(); row++) {
      // get the winning annotation for this cell
      final List<TCellAnnotation> winningCellAnnotations =
          annotations.getWinningContentCellAnnotation(row, subjectCol);

      // collect attributes from where candidate relations are created (e.g. for the entity "Graz" all attributes obtained from the KB)
      final List<Attribute> collectedAttributes = new ArrayList<>();
      for (final TCellAnnotation cellAnnotation : winningCellAnnotations) {
        for (Attribute attr : cellAnnotation.getAnnotation().getAttributes()) {
          int ind = attr.getValue().indexOf("^^");
          if (ind > 0) {
            attr.setValue(attr.getValue().substring(0, ind));
            attr.setValueURI(null);
          }
          collectedAttributes.add(attr);
        }
      }

      // collect cell values on the same row, from other columns
      final Map<Integer, String> cellValuesToMatch = new HashMap<>();
      for (final int col : columnDataTypes.keySet()) {
        if ((col != subjectCol)
            && !isRelationSuggested(subjectCol, col, constraints.getColumnRelations())) {
          final String cellValue = table.getContentCell(row, col).getText();
          cellValuesToMatch.put(col, cellValue);
        }
      }

      // perform matching and scoring
      // key=col id; value: contains the attr that matched with the highest score against cell in
      // that column
      final Map<Integer, List<Pair<Attribute, Double>>> cellMatchScores =
          this.attributeValueMatcher.match(collectedAttributes, cellValuesToMatch, columnDataTypes);

      for (final Map.Entry<Integer, List<Pair<Attribute, Double>>> e : cellMatchScores.entrySet()) {
        final RelationColumns subCol_to_objCol = new RelationColumns(subjectCol, e.getKey());

        final List<Pair<Attribute, Double>> matchedAttributes = e.getValue();
        for (final Pair<Attribute, Double> entry : matchedAttributes) {
          final String relationURI = entry.getKey().getRelationURI();
          final String relationLabel = entry.getKey().getRelationLabel();
          final List<Attribute> matchedValues = new ArrayList<>();
          matchedValues.add(entry.getKey());
          final TCellCellRelationAnotation cellcellRelation = new TCellCellRelationAnotation(
              subCol_to_objCol, row, relationURI, relationLabel, matchedValues, entry.getValue());
          annotations.addCellCellRelation(cellcellRelation);
        }
      }
    }
  }

  public RelationScorer getRelationScorer() {
    return this.relationScorer;
  }

  private boolean isRelationSuggested(final int subjectCol, final int objectCol,
      final Set<ColumnRelation> columnRelations) {
    for (final ColumnRelation relation : columnRelations) {
      if ((relation.getPosition().getFirstIndex() == subjectCol)
          && (relation.getPosition().getSecondIndex() == objectCol)) {
        this.suggestedRelationPositionsVisited++;
        return true;
      }
    }
    return false;
  }

  public int runRelationEnumeration(final TAnnotation annotations, final Table table,
      final int subjectCol, final Constraints constraints) throws STIException {
    this.suggestedRelationPositionsVisited = 0;
    generateCellCellRelations(annotations, table, subjectCol, constraints);
    // now we have created relation annotations per row, consolidate them to create column-column
    // relation
    enumerateColumnColumnRelation(annotations, table, constraints);
    return annotations.getCellcellRelations().size() + this.suggestedRelationPositionsVisited;
  }

}
