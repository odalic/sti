package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.kbproxy.KBProxyResult;
import uk.ac.shef.dcs.kbproxy.KnowledgeBaseProxy;
import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentCellRanker;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.stopping.StoppingCriteria;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.stopping.StoppingCriteriaInstantiator;
import uk.ac.shef.dcs.sti.core.extension.annotations.EntityCandidate;
import uk.ac.shef.dcs.sti.core.extension.constraints.Classification;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.DisambiguationResult;
import uk.ac.shef.dcs.sti.core.model.EntityResult;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.util.Pair;

/**
 * this class creates preliminary classification and disambiguation on a column
 */


public class LEARNINGPreliminaryColumnClassifier {
  private static final Logger LOG =
      LoggerFactory.getLogger(LEARNINGPreliminaryColumnClassifier.class.getName());
  private final TContentCellRanker selector;
  private final KnowledgeBaseProxy kbSearch;
  private final TCellDisambiguator cellDisambiguator;

  private final TColumnClassifier columnClassifier;
  private final String stopperClassname;
  private final String[] stopperParams;

  public LEARNINGPreliminaryColumnClassifier(final TContentCellRanker selector,
      final String stoppingCriteriaClassname, final String[] stoppingCriteriaParams,
      final KnowledgeBaseProxy candidateFinder, final TCellDisambiguator cellDisambiguator,
      final TColumnClassifier columnClassifier) {
    this.selector = selector;
    this.kbSearch = candidateFinder;
    this.cellDisambiguator = cellDisambiguator;
    this.columnClassifier = columnClassifier;

    this.stopperClassname = stoppingCriteriaClassname;
    this.stopperParams = stoppingCriteriaParams;
  }

  // assigns highest scoring clazz to the column;
  private void generatePreliminaryColumnClazz(final Map<Object, Double> state,
      final TAnnotation tableAnnotation, final int column) {
    if (state.size() > 0) {
      final List<Object> candidateClazz = new ArrayList<>(state.keySet());
      Collections.sort(candidateClazz, (o1, o2) -> state.get(o2).compareTo(state.get(o1)));
      // insert column type annotations
      final TColumnHeaderAnnotation[] preliminaryRankedCandidateClazz =
          new TColumnHeaderAnnotation[candidateClazz.size()];
      for (int i = 0; i < candidateClazz.size(); i++) {
        preliminaryRankedCandidateClazz[i] = (TColumnHeaderAnnotation) candidateClazz.get(i);
      }
      tableAnnotation.setHeaderAnnotation(column, preliminaryRankedCandidateClazz);
    }
  }


  /**
   * @param table
   * @param tableAnnotation
   * @param column
   * @param skipRows
   * @return pair: key is the index of the cell by which the classification stopped. value is the
   *         re-ordered indexes of cells based on the sampler
   * @throws KBProxyException
   */
  public Pair<Integer, List<List<Integer>>> runPreliminaryColumnClassifier(final Table table,
      final TAnnotation tableAnnotation, final int column, final Constraints constraints,
      final Integer... skipRows) throws ClassNotFoundException, STIException {
    final StoppingCriteria stopper =
        StoppingCriteriaInstantiator.instantiate(this.stopperClassname, this.stopperParams);

    // 1. gather list of strings from this column to be interpreted, rank them (for sampling)
    final List<List<Integer>> ranking =
        this.selector.select(table, column, tableAnnotation.getSubjectColumn());

    // 2. computeElementScores column and also disambiguate initial rows in the selected sample
    final List<TColumnHeaderAnnotation> headerClazzScores = new ArrayList<>();

    int countProcessed = 0, totalRows = 0;

    // 3. (added): if the classification is suggested by the user, then set it and return
    for (final Classification classification : constraints.getClassifications()) {
      if ((classification.getPosition().getIndex() == column)
          && !classification.getAnnotation().getChosen().isEmpty()) {
        for (final EntityCandidate suggestion : classification.getAnnotation().getChosen()) {
          headerClazzScores.add(new TColumnHeaderAnnotation(
              table.getColumnHeader(column).getHeaderText(),
              new Clazz(suggestion.getEntity().getResource(), suggestion.getEntity().getLabel()),
              suggestion.getScore().getValue()));
        }
        tableAnnotation.setHeaderAnnotation(column,
            headerClazzScores.toArray(new TColumnHeaderAnnotation[headerClazzScores.size()]));
        return new Pair<>(countProcessed, ranking);
      }
    }

    boolean stopped = false;
    Map<Object, Double> state = new HashMap<>();

    LOG.info("\t>> (LEARNING) Preliminary Column Classification begins");
    for (final List<Integer> blockOfRows : ranking) {
      countProcessed++;
      totalRows += blockOfRows.size();
      // find candidate entities
      final TCell sample = table.getContentCell(blockOfRows.get(0), column);
      if (sample.getText().length() < 2) {
        LOG.debug("\t\t>>> Very short text cell skipped: " + blockOfRows + "," + column + " "
            + sample.getText());
        continue;
      }

      LOG.info("\t\t>> cold start disambiguation, row(s) " + blockOfRows + "/" + ranking.size()
          + "," + sample);

      boolean skip = false;
      for (final int row : skipRows) {
        if (blockOfRows.contains(row)) {
          skip = true;
          break;
        }
      }

      DisambiguationResult entityScoresForBlock;
      if (skip) {
        entityScoresForBlock =
            new DisambiguationResult(toScoreMap(tableAnnotation, blockOfRows, column));
      } else {
        final EntityResult entityResult =
            constraints.getDisambChosenForCell(column, blockOfRows.get(0), this.kbSearch);
        List<Entity> candidates = entityResult.getResult();

        final List<String> warnings = entityResult.getWarnings();

        if (candidates.isEmpty()) {
          final KBProxyResult<List<Entity>> candidatesResult =
              this.kbSearch.findEntityCandidates(sample.getText());

          candidates = candidatesResult.getResult();
          candidatesResult.appendWarning(warnings);
        }

        // do cold start disambiguation
        entityScoresForBlock =
            this.cellDisambiguator.coldstartDisambiguate(candidates, table, blockOfRows, column);
        this.cellDisambiguator.addCellAnnotation(table, tableAnnotation, blockOfRows, column,
            entityScoresForBlock);
      }

      // run algorithm to runPreliminaryColumnClassifier column classification; header annotation
      // scores are updated constantly, but supporting rows are not.
      final Map<TColumnHeaderAnnotation, Double> scores =
          this.columnClassifier.generateCandidateClazz(entityScoresForBlock.getResult(),
              headerClazzScores, table, blockOfRows, column, totalRows);
      headerClazzScores.clear();
      headerClazzScores.addAll(scores.keySet());
      state = new HashMap<>();
      state.putAll(scores);

      final boolean stop = stopper.stop(state, table.getNumRows());

      if (stop) {
        LOG.info("\t>> (LEARNING) Preliminary Column Classification converged, rows:" + totalRows
            + "/" + ranking.size());
        // state is stable. annotate using the type, and disambiguate entities
        generatePreliminaryColumnClazz(state, tableAnnotation, column);
        stopped = true;
        break; // exit loop
      }
    }

    if (!stopped) {
      LOG.info("\t>> Preliminary Column Classification no convergence");
      generatePreliminaryColumnClazz(state, tableAnnotation, column); // supporting rows not added
    }
    return new Pair<>(countProcessed, ranking);
  }


  //
  private List<Pair<Entity, Map<String, Double>>> toScoreMap(final TAnnotation tableAnnotation,
      final List<Integer> blockOfRows, final int column) {
    final List<Pair<Entity, Map<String, Double>>> candidates = new ArrayList<>();
    for (final int row : blockOfRows) {
      final TCellAnnotation[] annotations = tableAnnotation.getContentCellAnnotations(row, column);
      for (final TCellAnnotation can : annotations) {
        final Entity ec = can.getAnnotation();
        final Map<String, Double> scoreElements = can.getScoreElements();
        scoreElements.put(TCellAnnotation.SCORE_FINAL, can.getFinalScore());
        candidates.add(new Pair<>(ec, scoreElements));
      }
    }
    return candidates;
  }



}
