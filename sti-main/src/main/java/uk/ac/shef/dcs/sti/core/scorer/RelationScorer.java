package uk.ac.shef.dcs.sti.core.scorer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.model.TCellCellRelationAnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnColumnRelationAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

/**
 */
public interface RelationScorer {

  List<TColumnColumnRelationAnnotation> computeElementScores(
      List<TCellCellRelationAnotation> cellcellRelationsOnRow,
      Collection<TColumnColumnRelationAnnotation> output, int subjectCol, int objectCol,
      Table table) throws STIException;

  Map<String, Double> computeFinal(TColumnColumnRelationAnnotation relation, int tableRowsTotal);

  List<TColumnColumnRelationAnnotation> computeRCScore(
      Collection<TColumnColumnRelationAnnotation> candidates, Table table, int column)
      throws STIException;

  List<TColumnColumnRelationAnnotation> computeREScore(
      List<TCellCellRelationAnotation> cellcellRelationAnotations,
      Collection<TColumnColumnRelationAnnotation> output, int subjectCol, int objectCol)
      throws STIException;

  double scoreDC(TColumnColumnRelationAnnotation hbr, List<String> domain_representation)
      throws STIException;
}
