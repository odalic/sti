package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.KBProxyResult;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.DisambiguationResult;
import uk.ac.shef.dcs.sti.core.model.EntityResult;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

/**
 */
public class LEARNINGPreliminaryDisamb {

  private static final Logger LOG =
      LoggerFactory.getLogger(LEARNINGPreliminaryDisamb.class.getName());
  private final TCellDisambiguator disambiguator;
  private final KBProxy kbSearch;
  private final TColumnClassifier classifier;

  public LEARNINGPreliminaryDisamb(final KBProxy kbSearch, final TCellDisambiguator disambiguator,
      final TColumnClassifier classifier) {
    this.kbSearch = kbSearch;
    this.disambiguator = disambiguator;
    this.classifier = classifier;
  }

  // search candidates for the cell;
  // computeElementScores candidates for the cell;
  // create annotation and update supportin header and header computeElementScores (depending on the
  // two params updateHeader_blah
  private DisambiguationResult constrainedDisambiguate(final TCell tcc, final Table table,
      final Set<String> winningColumnClazz, final List<Integer> rowBlock, final int column,
      final int totalRowBlocks, final Constraints constraints) {
    DisambiguationResult entity_and_scoreMap;

    final EntityResult entityResult =
        constraints.getDisambChosenForCell(column, rowBlock.get(0), this.kbSearch);;
    List<Entity> candidates = entityResult.getResult();
    final List<String> warnings = entityResult.getWarnings();

    if (candidates.isEmpty()) {
      final KBProxyResult<List<Entity>> candidatesResult = this.kbSearch
          .findEntityCandidatesOfTypes(tcc.getText(), winningColumnClazz.toArray(new String[0]));

      candidates = candidatesResult.getResult();
      candidatesResult.appendWarning(warnings);
    }

    if (candidates.isEmpty()) {
      final KBProxyResult<List<Entity>> candidatesResult =
          this.kbSearch.findEntityCandidatesOfTypes(tcc.getText());

      candidates = candidatesResult.getResult();
      candidatesResult.appendWarning(warnings);
    }

    // now each candidate is given scores
    entity_and_scoreMap = this.disambiguator.constrainedDisambiguate(candidates, table, rowBlock,
        column, totalRowBlocks, true);

    entity_and_scoreMap.getWarnings().addAll(warnings);

    return entity_and_scoreMap;
  }

  // for those cells already processed in preliminary column classification,
  // preliminary disamb simply reselects entities whose type overlap with winning column clazz
  // annotation
  private void reselect(final TAnnotation tableAnnotation, final int stopPointByPreColumnClassifier,
      final List<List<Integer>> cellBlockRanking, final Collection<String> winningClazzIds,
      final int column) {
    final TColumnHeaderAnnotation[] headers = tableAnnotation.getHeaderAnnotation(column);
    for (int index = 0; index < stopPointByPreColumnClassifier; index++) {
      final List<Integer> cellBlock = cellBlockRanking.get(index);
      for (final int row : cellBlock) {
        final TCellAnnotation[] cellAnnotations =
            tableAnnotation.getContentCellAnnotations(row, column);
        final TCellAnnotation[] revised =
            this.disambiguator.reselect(cellAnnotations, winningClazzIds);
        if (revised.length != 0) {
          tableAnnotation.setContentCellAnnotations(row, column, revised);
        }

        // now update supporting rows for the elected column clazz
        if (headers != null) {
          for (final TColumnHeaderAnnotation ha : headers) {
            for (final TCellAnnotation tca : revised) {
              if (tca.getAnnotation().getTypes().contains(ha.getAnnotation())) {
                ha.addSupportingRow(row);
                break;
              }
            }
          }
        }
      }
    }
  }


  public void runPreliminaryDisamb(final int stopPointByPreColumnClassifier,
      final List<List<Integer>> ranking, final Table table, final TAnnotation tableAnnotation,
      final int column, final Constraints constraints, final Integer... skipRows)
      throws STIException {

    LOG.info("\t>> (LEARNING) Preliminary Disambiguation begins");
    final List<TColumnHeaderAnnotation> winningColumnClazz =
        tableAnnotation.getWinningHeaderAnnotations(column);
    final Set<String> winningColumnClazzIds = new HashSet<>();
    for (final TColumnHeaderAnnotation ha : winningColumnClazz) {
      winningColumnClazzIds.add(ha.getAnnotation().getId());
    }

    // for those cells already processed by pre column classification, update their cell annotations
    LOG.info("\t\t>> re-annotate cells involved in cold start disambiguation");
    reselect(tableAnnotation, stopPointByPreColumnClassifier, ranking, winningColumnClazzIds,
        column);

    // for remaining...
    LOG.info("\t\t>> constrained cell disambiguation for the rest cells in this column");
    final int end = ranking.size();

    final List<Integer> updated = new ArrayList<>();
    for (int bi = stopPointByPreColumnClassifier; bi < end; bi++) {
      final List<Integer> rows = ranking.get(bi);

      boolean skip = false;
      for (final int i : skipRows) {
        if (rows.contains(i)) {
          skip = true;
          break;
        }
      }
      if (skip) {
        continue;
      }

      final TCell sample = table.getContentCell(rows.get(0), column);
      if (sample.getText().length() < 2) {
        LOG.debug(
            "\t\t>>> short text cell skipped: " + rows + "," + column + " " + sample.getText());
        continue;
      }

      final DisambiguationResult entity_and_scoreMap = constrainedDisambiguate(sample, table,
          winningColumnClazzIds, rows, column, ranking.size(), constraints);

      if (entity_and_scoreMap.getResult().size() > 0) {
        this.disambiguator.addCellAnnotation(table, tableAnnotation, rows, column,
            entity_and_scoreMap);
        updated.addAll(rows);
      } else {
        for (final int row : rows) {
          tableAnnotation.addContentWarnings(row, column, entity_and_scoreMap.getWarnings());
        }
      }
    }

    LOG.info("\t\t>> constrained cell disambiguation complete " + updated.size() + "/"
        + ranking.size() + " rows");
    LOG.info("\t\t>> reset candidate column class annotations");
    this.classifier.updateColumnClazz(updated, column, tableAnnotation, table, false);

  }
}
