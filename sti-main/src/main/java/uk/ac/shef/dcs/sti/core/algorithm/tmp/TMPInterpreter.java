package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.util.Pair;

/**

 */
public class TMPInterpreter extends SemanticTableInterpreter {

  private static final Logger LOG = LoggerFactory.getLogger(TMPInterpreter.class.getName());
  private final SubjectColumnDetector subjectColumnDetector;
  private final LEARNING learning;
  private final LiteralColumnTagger literalColumnTagger;
  private final TColumnColumnRelationEnumerator relationEnumerator;

  private final UPDATE update;

  public TMPInterpreter(final SubjectColumnDetector subjectColumnDetector, final LEARNING learning,
      final UPDATE update, final TColumnColumnRelationEnumerator relationEnumerator,
      final LiteralColumnTagger literalColumnTagger, final int[] ignoreColumns,
      final int[] mustdoColumns) {
    super(ignoreColumns, mustdoColumns);
    this.subjectColumnDetector = subjectColumnDetector;
    this.learning = learning;
    this.literalColumnTagger = literalColumnTagger;
    this.relationEnumerator = relationEnumerator;

    this.update = update;
  }

  @Override
  public TAnnotation start(final Table table, final boolean relationLearning) throws STIException {
    // 1. find the main subject column of this table
    LOG.info(">\t PHASE: Detecting subject column...");
    final int[] ignoreColumnsArray = new int[getIgnoreColumns().size()];

    int index = 0;
    for (final Integer i : getIgnoreColumns()) {
      ignoreColumnsArray[index] = i;
      index++;
    }
    try {
      final List<Pair<Integer, Pair<Double, Boolean>>> subjectColumnScores =
          this.subjectColumnDetector.compute(table, ignoreColumnsArray);

      final TAnnotation tableAnnotations = new TAnnotation(table.getNumRows(), table.getNumCols());
      tableAnnotations.setSubjectColumns(new HashSet<>(Arrays.asList(subjectColumnScores.get(0).getKey())));

      final List<Integer> annotatedColumns = new ArrayList<>();
      LOG.info(">\t PHASE: LEARNING ...");
      for (int col = 0; col < table.getNumCols(); col++) {
        /*
         * if(col!=1) continue;
         */
        if (isCompulsoryColumn(col)) {
          LOG.info("\t>> Column=(compulsory)" + col);
          annotatedColumns.add(col);
          this.learning.learn(table, tableAnnotations, col);
        } else {
          if (getIgnoreColumns().contains(col)) {
            continue;
          }
          if (!table.getColumnHeader(col).getFeature().getMostFrequentDataType().getType()
              .equals(DataTypeClassifier.DataType.NAMED_ENTITY)) {
            continue;
          }
          /*
           * if (table.getColumnHeader(col).getFeature().isAcronymColumn()) continue;
           */
          annotatedColumns.add(col);

          // if (tab_annotations.getRelationAnnotationsBetween(main_subject_column, col) == null) {
          LOG.info("\t>> Column=" + col);
          this.learning.learn(table, tableAnnotations, col);
        }
      }

      if (this.update != null) {
        LOG.info(">\t PHASE: UPDATE phase ...");
        this.update.update(annotatedColumns, table, tableAnnotations);
      }
      if (relationLearning) {
        LOG.info("\t> PHASE: RELATION ENUMERATION ...");
        new RELATIONENUMERATION().enumerate(subjectColumnScores, getIgnoreColumns(),
            this.relationEnumerator, tableAnnotations, table, annotatedColumns, this.update);

        // 4. consolidation-for columns that have relation with main subject column, if the column
        // is
        // entity column, do column typing and disambiguation; otherwise, simply create header
        // annotation
        LOG.info("\t\t>> Annotate literal-columns in relation with main column");
        this.literalColumnTagger.annotate(table, tableAnnotations,
            annotatedColumns.toArray(new Integer[0]));
      }
      return tableAnnotations;
    } catch (final Exception e) {
      throw new STIException(e);
    }
  }

  @Override
  public TAnnotation start(final Table table, final boolean statistical,
      final Constraints constraints) throws STIException {
    return start(table, !statistical);
  }

}
