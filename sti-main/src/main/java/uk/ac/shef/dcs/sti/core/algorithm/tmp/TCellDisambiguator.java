package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.KBProxyResult;
import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.core.model.DisambiguationResult;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.scorer.EntityScorer;
import uk.ac.shef.dcs.util.Pair;

/**
 */
public class TCellDisambiguator {

  private static final Logger LOG = LoggerFactory.getLogger(TCellDisambiguator.class.getName());
  private final KBProxy kbSearch;
  private final EntityScorer disambScorer;

  public TCellDisambiguator(final KBProxy kbSearch, final EntityScorer disambScorer) {
    this.kbSearch = kbSearch;
    this.disambScorer = disambScorer;
  }

  protected void addCellAnnotation(final Table table, final TAnnotation tableAnnotation,
      final List<Integer> rowBlock, final int table_cell_col,
      final DisambiguationResult entities_and_scoreMap) {

    Collections.sort(entities_and_scoreMap.getResult(), (o1, o2) -> {
      final Double o2_score = o2.getValue().get(TCellAnnotation.SCORE_FINAL);
      final Double o1_score = o1.getValue().get(TCellAnnotation.SCORE_FINAL);
      return o2_score.compareTo(o1_score);
    });

    final String cellText = table.getContentCell(rowBlock.get(0), table_cell_col).getText();
    for (final int row : rowBlock) {
      final TCellAnnotation[] annotationsForCell =
          new TCellAnnotation[entities_and_scoreMap.getResult().size()];
      for (int i = 0; i < entities_and_scoreMap.getResult().size(); i++) {
        final Pair<Entity, Map<String, Double>> e = entities_and_scoreMap.getResult().get(i);
        final double score = e.getValue().get(TCellAnnotation.SCORE_FINAL);
        annotationsForCell[i] = new TCellAnnotation(cellText, e.getKey(), score, e.getValue());

      }

      tableAnnotation.setContentCellAnnotations(row, table_cell_col, annotationsForCell);
      tableAnnotation.addContentWarnings(row, table_cell_col, entities_and_scoreMap.getWarnings());
    }
  }

  // this method runs cold start disambiguation
  public DisambiguationResult coldstartDisambiguate(final List<Entity> candidates,
      final Table table, final List<Integer> entity_rows, final int entity_column) {
    LOG.info("\t\t>> (cold start disamb), candidates=" + candidates.size());
    return disambiguate(candidates, table, entity_rows, entity_column);
  }

  public DisambiguationResult constrainedDisambiguate(final List<Entity> candidates,
      final Table table, final List<Integer> rowBlock, final int column, final int totalRowBlocks,
      final boolean isLEARNINGPhase) {
    final TCell sample_tcc = table.getContentCell(rowBlock.get(0), column);
    if (isLEARNINGPhase) {
      LOG.info("\t\t>> (constrained disambiguation in LEARNING) , position at (" + rowBlock + "/"
          + totalRowBlocks + "," + column + ") " + sample_tcc + " candidates=" + candidates.size());
    } else {
      LOG.info("\t\t>> (constrained disambiguation in UPDATE), position at (" + rowBlock + "/"
          + totalRowBlocks + "," + column + ") " + sample_tcc + " (candidates)-"
          + candidates.size());
    }

    return disambiguate(candidates, table, rowBlock, column);
  }

  public DisambiguationResult disambiguate(final List<Entity> candidates, final Table table,
      final List<Integer> entity_rows, final int entity_column) {
    // do disambiguation scoring
    // LOG.info("\t>> Disambiguation-LEARN, position at (" + entity_row + "," + entity_column + ")
    // candidates=" + candidates.size());
    final TCell sample_tcc = table.getContentCell(entity_rows.get(0), entity_column);
    final List<Pair<Entity, Map<String, Double>>> disambiguationScores = new ArrayList<>();
    final List<String> warnings = new ArrayList<>();

    for (final Entity c : candidates) {
      // find facts of each entity
      if ((c.getAttributes() == null) || (c.getAttributes().size() == 0)) {
        final KBProxyResult<List<Attribute>> attributesResult =
            this.kbSearch.findAttributesOfEntities(c);

        c.setAttributes(attributesResult.getResult());
        attributesResult.appendWarning(warnings);
      }
      final Map<String, Double> scoreMap = this.disambScorer.computeElementScores(c, candidates,
          entity_column, entity_rows.get(0), entity_rows, table);
      this.disambScorer.computeFinal(scoreMap, sample_tcc.getText());
      final Pair<Entity, Map<String, Double>> entry = new Pair<>(c, scoreMap);
      disambiguationScores.add(entry);
    }
    return new DisambiguationResult(disambiguationScores, warnings);
  }

  // reselect cell entities for this cell ensuring their types are contained in the winning clazz
  // for the column
  public TCellAnnotation[] reselect(final TCellAnnotation[] existingCellAnnotations,
      final Collection<String> winningClazzIdsForColumn) {
    final List<TCellAnnotation> selected = new ArrayList<>();
    for (final TCellAnnotation tca : existingCellAnnotations) {
      final int overlap = CollectionUtils
          .intersection(tca.getAnnotation().getTypeIds(), winningClazzIdsForColumn).size();
      if (overlap > 0) {
        selected.add(tca);
      }
    }

    return selected.toArray(new TCellAnnotation[0]);
  }

}
