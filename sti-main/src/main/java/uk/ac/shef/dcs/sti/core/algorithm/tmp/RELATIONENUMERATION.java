package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.MLPreClassification;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.extension.positions.ColumnPosition;
import uk.ac.shef.dcs.sti.core.model.RelationColumns;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnColumnRelationAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.scorer.RelationScorer;
import uk.ac.shef.dcs.sti.core.subjectcol.TColumnFeature;
import uk.ac.shef.dcs.util.Pair;

/**
 * Created by - on 02/04/2016.
 */
public class RELATIONENUMERATION {
  private static final Logger LOG = LoggerFactory.getLogger(RELATIONENUMERATION.class.getName());

  public void enumerate(final List<Pair<Integer, Pair<Double, Boolean>>> subjectColCandidadteScores,
      final Set<Integer> ignoreCols, final TColumnColumnRelationEnumerator relationEnumerator,
      final TAnnotation tableAnnotations, final Table table, final List<Integer> annotatedColumns,
      final UPDATE update) throws STIException {

    enumerate(subjectColCandidadteScores, ignoreCols, relationEnumerator, tableAnnotations, table,
        annotatedColumns, update, new MLPreClassification(), new Constraints());
  }

  public void enumerate(final List<Pair<Integer, Pair<Double, Boolean>>> subjectColCandidadteScores,
                        final Set<Integer> ignoreCols, final TColumnColumnRelationEnumerator relationEnumerator,
                        TAnnotation tableAnnotations, final Table table, final List<Integer> annotatedColumns,
                        final UPDATE update, final MLPreClassification mlPreClassification,
                        final Constraints constraints) throws STIException {
    double winningSolutionScore = 0;
    int subjectCol;
    TAnnotation winningSolution = null;
    for (final Pair<Integer, Pair<Double, Boolean>> mainCol : subjectColCandidadteScores) {
      // tab_annotations = new TAnnotation(table.getNumRows(), table.getNumCols());
      subjectCol = mainCol.getKey();
      if (ignoreCols.contains(subjectCol)) {
        continue;
      }

      LOG.info(">>\t\t Let subject column=" + subjectCol);
      final int relatedColumns = relationEnumerator.runRelationEnumeration(tableAnnotations, table,
          subjectCol, mlPreClassification, constraints);

      boolean interpretable = false;
      if (relatedColumns > 0) {
        interpretable = true;
      }

      if (interpretable) {
        tableAnnotations.setSubjectColumn(subjectCol);
        break;
      } else {
        // the current subject column could be wrong, try differently
        final double overallScore = scoreSolution(tableAnnotations, table, subjectCol);
        if (overallScore > winningSolutionScore) {
          tableAnnotations.setSubjectColumn(subjectCol);
          winningSolution = tableAnnotations;
          winningSolutionScore = overallScore;
        }
        tableAnnotations.resetRelationAnnotations();
        LOG.warn(
            ">>\t\t (this subject column does not form relation with other columns, try the next column)");
        continue;
      }
    }
    if ((tableAnnotations == null) && (winningSolution != null)) {
      tableAnnotations = winningSolution;
    }

    if (STIConstantProperty.REVISE_RELATION_ANNOTATION_BY_DC && (update != null)) {
      final List<String> domain_rep =
          update.createDomainRep(table, tableAnnotations, annotatedColumns);
      reviseColumnColumnRelationAnnotations(tableAnnotations, domain_rep,
          relationEnumerator.getRelationScorer());
    }
  }

  public void enumerate(final TColumnColumnRelationEnumerator relationEnumerator,
      TAnnotation tableAnnotations, final Table table, final List<Integer> annotatedColumns,
      final UPDATE update, final MLPreClassification mlPreClassification, final Constraints constraints) throws STIException {
    Set<Integer> subjectColumns = new HashSet<>();

    for (ColumnPosition subjectPosition : constraints.getSubjectColumnsPositionsSorted()) {
      LOG.info(">>\t\t Let subject column=" + subjectPosition.getIndex());
      relationEnumerator.runRelationEnumeration(tableAnnotations, table,
          subjectPosition.getIndex(), mlPreClassification, constraints);
      subjectColumns.add(subjectPosition.getIndex());
    }
    tableAnnotations.setSubjectColumns(subjectColumns);

    if (STIConstantProperty.REVISE_RELATION_ANNOTATION_BY_DC && (update != null)) {
      final List<String> domain_rep =
          update.createDomainRep(table, tableAnnotations, annotatedColumns);
      reviseColumnColumnRelationAnnotations(tableAnnotations, domain_rep,
          relationEnumerator.getRelationScorer());
    }
  }

  private void reviseColumnColumnRelationAnnotations(final TAnnotation annotation,
      final List<String> domain_representation, final RelationScorer relationScorer)
      throws STIException {
    for (final Map.Entry<RelationColumns, List<TColumnColumnRelationAnnotation>> entry : annotation
        .getColumncolumnRelations().entrySet()) {

      for (final TColumnColumnRelationAnnotation relation : entry.getValue()) {
        final double domain_consensus = relationScorer.scoreDC(relation, domain_representation);
        relation.setFinalScore(relation.getFinalScore() + domain_consensus);
      }
      Collections.sort(entry.getValue());
    }
  }

  private double scoreSolution(final TAnnotation tableAnnotations, final Table table,
      final int subjectColumn) {
    double entityScores = 0.0;
    for (int col = 0; col < table.getNumCols(); col++) {
      for (int row = 0; row < table.getNumRows(); row++) {
        final TCellAnnotation[] cAnns = tableAnnotations.getContentCellAnnotations(row, col);
        if ((cAnns != null) && (cAnns.length > 0)) {
          entityScores += cAnns[0].getFinalScore();
        }
      }
    }

    double relationScores = 0.0;
    for (final Map.Entry<RelationColumns, List<TColumnColumnRelationAnnotation>> entry : tableAnnotations
        .getColumncolumnRelations().entrySet()) {
      entry.getKey();
      final TColumnColumnRelationAnnotation rel = entry.getValue().get(0);
      relationScores += rel.getFinalScore();
    }
    final TColumnFeature cf = table.getColumnHeader(subjectColumn).getFeature();
    // relationScores = relationScores * cf.getValueDiversity();

    final double diversity = cf.getUniqueCellCount() + cf.getUniqueTokenCount();
    return (entityScores + relationScores) * diversity
        * ((table.getNumRows() - cf.getEmptyCellCount()) / (double) table.getNumRows());
  }

}
