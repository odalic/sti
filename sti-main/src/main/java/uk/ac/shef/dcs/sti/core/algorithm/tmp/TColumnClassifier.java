package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.kbproxy.Proxy;
import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPClazzScorer;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.scorer.ClazzScorer;
import uk.ac.shef.dcs.util.Pair;

/**
 * Created by - on 04/04/2016.
 */
public class TColumnClassifier {

  private static final Logger LOG = LoggerFactory.getLogger(TColumnClassifier.class.getName());
  private final ClazzScorer clazzScorer;

  public TColumnClassifier(final ClazzScorer scorer) {
    this.clazzScorer = scorer;
  }

  public Map<TColumnHeaderAnnotation, Double> generateCandidateClazz(
      final List<Pair<Entity, Map<String, Double>>> entityScoresForBlock,
      final List<TColumnHeaderAnnotation> existingColumnClazzCandidates, final Table table,
      final List<Integer> blockOfRows, final int column, final int tableRowsTotal,
      final Proxy kbProxy)
      throws STIException {
    final Collection<TColumnHeaderAnnotation> candidateHeaderAnnotations =
        this.clazzScorer.computeElementScores(entityScoresForBlock, existingColumnClazzCandidates,
            table, blockOfRows, column, kbProxy);

    LOG.info("\t\t>> update candidate clazz on column, existing="
        + existingColumnClazzCandidates.size());
    final Map<TColumnHeaderAnnotation, Double> state = new HashMap<>();
    for (final TColumnHeaderAnnotation ha : candidateHeaderAnnotations) {
      // Map<String, Double> scoreElements =ha.getScoreElements();
      this.clazzScorer.computeFinal(ha, tableRowsTotal);
      state.put(ha, ha.getFinalScore());
    }
    return state;
  }

  /**
   * given a cell annotation, and existing TColumnHeaderAnnotations on the column, go through the
   * types (clazz) of that cell annotation, select the ones that are not included in the existing
   * TColumnHeaderAnnotations
   *
   * @param ca
   * @param column
   * @param table
   * @param existingColumnClazzAnnotations
   * @return
   */
  private List<TColumnHeaderAnnotation> selectNew(final TCellAnnotation ca, final int column,
      final Table table, final Collection<TColumnHeaderAnnotation> existingColumnClazzAnnotations) {

    final List<Clazz> types = ca.getAnnotation().getTypes();

    final List<TColumnHeaderAnnotation> selected = new ArrayList<>();
    for (int index = 0; index < types.size(); index++) {
      boolean found = false;
      final Clazz type = types.get(index);
      for (final TColumnHeaderAnnotation ha : existingColumnClazzAnnotations) {
        if (type.equals(ha.getAnnotation())) {
          found = true;
          break;
        }
      }
      if (!found) {
        final TColumnHeaderAnnotation ha =
            new TColumnHeaderAnnotation(table.getColumnHeader(column).getHeaderText(), type, 0.0);
        selected.add(ha);
      }
    }
    return selected;
  }


  protected void updateClazzScoresByDC(final TAnnotation currentAnnotation,
      final List<String> domanRep, final List<Integer> interpretedColumns) throws STIException {
    for (final int c : interpretedColumns) {
      final List<TColumnHeaderAnnotation> headers =
          new ArrayList<>(Arrays.asList(currentAnnotation.getHeaderAnnotation(c)));

      for (final TColumnHeaderAnnotation ha : headers) {
        final double dc = this.clazzScorer.computeDC(ha, domanRep);
        ha.setFinalScore(ha.getFinalScore() + dc);
      }

      Collections.sort(headers);
      currentAnnotation.setHeaderAnnotation(c, headers.toArray(new TColumnHeaderAnnotation[0]));
    }
  }


  /**
   * after disamb on the column, go thru the cells that have been newly disambiguated (i.e., in
   * addition to cold start disamb) update class annotation for the column due to these new cells
   *
   * @param rowsUpdated
   * @param column
   * @param tableAnnotations
   * @param table
   * @param resetCESums if true, the sum_ce and sum_vote will be set to 0, before the newly
   *        disambiguated rows in rowsUpdated are counted
   */
  protected void updateColumnClazz(final List<Integer> rowsUpdated, final int column,
      final TAnnotation tableAnnotations, final Table table, final boolean resetCESums)
      throws STIException {
    List<TColumnHeaderAnnotation> existingColumnClazzAnnotations;
    existingColumnClazzAnnotations =
        tableAnnotations.getHeaderAnnotation(column) == null ? new ArrayList<>()
            : new ArrayList<>(Arrays.asList(tableAnnotations.getHeaderAnnotation(column)));

    // supporting rows are added if a header for the type of the cell annotation exists
    final List<TColumnHeaderAnnotation> toAdd = new ArrayList<>();
    // deal with newly disambiguated cells (that is, in addition to cold start disamb)
    for (final int row : rowsUpdated) {
      final List<TCellAnnotation> winningEntities =
          tableAnnotations.getWinningContentCellAnnotation(row, column);
      for (final TCellAnnotation ca : winningEntities) {
        for (final TColumnHeaderAnnotation ha : selectNew(ca, column, table,
            existingColumnClazzAnnotations)) {
          if (!toAdd.contains(ha)) {
            toAdd.add(ha);
          }
        }
      }
    }

    toAdd.addAll(existingColumnClazzAnnotations);
    final TColumnHeaderAnnotation[] result =
        updateColumnClazzAnnotationScores(rowsUpdated, column, table.getNumRows(),
            existingColumnClazzAnnotations, table, tableAnnotations, this.clazzScorer, resetCESums);
    tableAnnotations.setHeaderAnnotation(column, result);
  }

  /**
   * Used after disamb, to update candidate column clazz annotations on the column
   *
   * @param updatedRows
   * @param column
   * @param totalRows
   * @param candidateColumnClazzAnnotations
   * @param table
   * @param tableAnnotations
   * @param clazzScorer
   * @param resetCESums if true, the sum_ce and sum_vote will be set to 0, before the newly
   *        disambiguated rows in rowsUpdated are counted
   * @return
   */
  private TColumnHeaderAnnotation[] updateColumnClazzAnnotationScores(
      final Collection<Integer> updatedRows, final int column, final int totalRows,
      Collection<TColumnHeaderAnnotation> candidateColumnClazzAnnotations, final Table table,
      final TAnnotation tableAnnotations, final ClazzScorer clazzScorer, final boolean resetCESums)
      throws STIException {
    // for the candidate column clazz annotations compute CC score
    candidateColumnClazzAnnotations =
        clazzScorer.computeCCScore(candidateColumnClazzAnnotations, table, column);

    if (resetCESums) {
      for (final TColumnHeaderAnnotation ha : candidateColumnClazzAnnotations) {
        ha.getScoreElements().put(TMPClazzScorer.SUM_CELL_VOTE, 0.0);
        ha.getScoreElements().put(TMPClazzScorer.SUM_CE, 0.0);
      }
    }

    for (final int row : updatedRows) {
      final List<TCellAnnotation> winningEntities =
          tableAnnotations.getWinningContentCellAnnotation(row, column);
      final Set<String> votedClazzIdsByThisCell = new HashSet<>();
      for (final TCellAnnotation ca : winningEntities) {
        // go thru each candidate column clazz annotation, check if this winning entity has a type
        // that is this clazz
        for (final TColumnHeaderAnnotation ha : candidateColumnClazzAnnotations) {
          if (ca.getAnnotation().getTypes().contains(ha.getAnnotation())) {
            ha.addSupportingRow(row);
            if (!votedClazzIdsByThisCell.contains(ha.getAnnotation().getId())) {
              // update the CE score elements for this column clazz annotation
              Double sum_votes = ha.getScoreElements().get(TMPClazzScorer.SUM_CELL_VOTE);
              if (sum_votes == null) {
                sum_votes = 0.0;
              }
              sum_votes++;
              ha.getScoreElements().put(TMPClazzScorer.SUM_CELL_VOTE, sum_votes);

              Double sum_ce = ha.getScoreElements().get(TMPClazzScorer.SUM_CE);
              if (sum_ce == null) {
                sum_ce = 0.0;
              }
              sum_ce += ca.getFinalScore();
              ha.getScoreElements().put(TMPClazzScorer.SUM_CE, sum_ce);
              votedClazzIdsByThisCell.add(ha.getAnnotation().getId());
            }
          } else if (votedClazzIdsByThisCell.contains(ha.getAnnotation().getId())) {
          }

        }
      }
    }

    // finally recompute final scores, because CE scores could have changed
    final List<TColumnHeaderAnnotation> revised = new ArrayList<>();
    for (final TColumnHeaderAnnotation ha : candidateColumnClazzAnnotations) {
      clazzScorer.computeFinal(ha, totalRows);
      revised.add(ha);
    }

    Collections.sort(revised);
    return revised.toArray(new TColumnHeaderAnnotation[0]);
  }

}
