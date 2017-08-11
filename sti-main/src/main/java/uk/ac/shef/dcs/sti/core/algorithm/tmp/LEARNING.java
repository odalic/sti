package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import java.util.List;
import java.util.Set;

import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.util.Pair;

import cz.cuni.mff.xrg.odalic.util.logging.PerformanceLogger;

/**
 * Represents the LEARNING phase, creates preliminary column classification and cell disambiguation
 */
public class LEARNING {

  private final LEARNINGPreliminaryColumnClassifier columnTagger;
  private final LEARNINGPreliminaryDisamb cellTagger;
  private final PerformanceLogger performanceLogger;


  public LEARNING(final LEARNINGPreliminaryColumnClassifier columnTagger,
                  final LEARNINGPreliminaryDisamb cellTagger, PerformanceLogger performanceLogger) {
    this.columnTagger = columnTagger;
    this.cellTagger = cellTagger;
    this.performanceLogger = performanceLogger;
  }

  public void learn(final Table table, final TAnnotation tableAnnotation, final int column)
      throws ClassNotFoundException, STIException {
    learn(table, tableAnnotation, column, new Constraints());
  }

  public void learn(final Table table, final TAnnotation tableAnnotation, final int column,
      final Constraints constraints) throws ClassNotFoundException, STIException {
    final Set<Integer> skipRows = constraints.getSkipRowsForColumn(column, table.getNumRows());

    final Integer[] skipRowsArray = skipRows.toArray(new Integer[skipRows.size()]);

    final Pair<Integer, List<List<Integer>>> stopPosition = performanceLogger.<Pair<Integer, List<List<Integer>>>, ClassNotFoundException, STIException>doThrowableFunction2(
        "Interpreter - 2 - Preliminary classification",
        () -> this.columnTagger.runPreliminaryColumnClassifier(table, tableAnnotation, column, constraints, skipRowsArray));

    performanceLogger.doThrowableMethod("Interpreter - 2 - Preliminary disambiguation", () ->
        this.cellTagger.runPreliminaryDisamb(stopPosition.getKey(), stopPosition.getValue(), table,
        tableAnnotation, column, constraints, skipRowsArray));
  }

}
