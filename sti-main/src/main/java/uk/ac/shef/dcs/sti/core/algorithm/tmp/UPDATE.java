package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.kbproxy.ProxyException;
import uk.ac.shef.dcs.kbproxy.ProxyResult;
import uk.ac.shef.dcs.kbproxy.Proxy;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentCellRanker;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPClazzScorer;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.DisambiguationResult;
import uk.ac.shef.dcs.sti.core.model.EntityResult;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.util.StringUtils;

/**

 */
public class UPDATE {

  private static final Logger LOG = LoggerFactory.getLogger(UPDATE.class.getName());
  private final TCellDisambiguator disambiguator;
  private final Proxy kbSearch;
  private final TColumnClassifier classifier;
  private final String nlpResourcesDir;
  private final TContentCellRanker selector;
  private final List<String> stopWords;

  public UPDATE(final TContentCellRanker selector, final Proxy kbSearch,
      final TCellDisambiguator disambiguator, final TColumnClassifier classifier,
      final List<String> stopwords, final String nlpResourcesDir) {
    this.selector = selector;
    this.kbSearch = kbSearch;
    this.disambiguator = disambiguator;
    this.classifier = classifier;
    this.nlpResourcesDir = nlpResourcesDir;
    this.stopWords = stopwords;
  }

  private Set<String> collectAllEntityCandidateIds(final Table table,
      final TAnnotation prevAnnotation) {
    final Set<String> ids = new HashSet<>();
    for (int col = 0; col < table.getNumCols(); col++) {
      for (int row = 0; row < table.getNumRows(); row++) {
        final TCellAnnotation[] cas = prevAnnotation.getContentCellAnnotations(row, col);
        if (cas == null) {
          continue;
        }
        for (final TCellAnnotation ca : cas) {
          ids.add(ca.getAnnotation().getId());
        }
      }
    }
    return ids;
  }

  public List<String> createDomainRep(final Table table, final TAnnotation currentAnnotation,
      final List<Integer> interpretedColumns) {
    final List<String> domain = new ArrayList<>();
    for (final int c : interpretedColumns) {
      for (int r = 0; r < table.getNumRows(); r++) {
        final TCellAnnotation[] annotations = currentAnnotation.getContentCellAnnotations(r, c);
        if ((annotations != null) && (annotations.length > 0)) {
          final Entity ec = annotations[0].getAnnotation();
          try {
            domain.addAll(createEntityDomainRep(ec));
          } catch (final IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return domain;
  }

  private Collection<? extends String> createEntityDomainRep(final Entity ec) throws IOException {
    final List<String> domain = new ArrayList<>();
    final String desc = ec.getDescription(this.kbSearch.getDefinition());
    final String[] sentences =
        NLPTools.getInstance(this.nlpResourcesDir).getSentenceSplitter().sentDetect(desc);
    final String first = sentences.length > 0 ? sentences[0] : "";
    final List<String> tokens = StringUtils.toBagOfWords(first, true, true, true);
    domain.addAll(tokens);
    domain.removeAll(this.stopWords);
    return domain;
  }

  private DisambiguationResult disambiguate(final Set<String> ignoreEntityIds, final TCell tcc,
      final Table table, final Set<String> constrainedClazz, final List<Integer> rowBlock,
      final int table_cell_col, final int totalRowBlocks, final Constraints constraints) {
    DisambiguationResult entity_and_scoreMap;

    final EntityResult entityResult =
        constraints.getDisambChosenForCell(table_cell_col, rowBlock.get(0), this.kbSearch);
    List<Entity> candidates = entityResult.getResult();
    final List<String> warnings = entityResult.getWarnings();

    if (candidates.isEmpty()) {
      final ProxyResult<List<Entity>> candidatesResult = this.kbSearch
          .findEntityCandidatesOfTypes(tcc.getText(), constrainedClazz.toArray(new String[0]));

      candidates = candidatesResult.getResult();
      candidatesResult.appendExistingWarning(warnings);
    }

    int ignore = 0;
    for (final uk.ac.shef.dcs.kbproxy.model.Resource ec : candidates) {
      if (ignoreEntityIds.contains(ec.getId())) {
        ignore++;
      }
    }
    if (candidates.isEmpty()) {
      final ProxyResult<List<Entity>> candidatesResult =
          this.kbSearch.findEntityCandidatesOfTypes(tcc.getText());

      candidates = candidatesResult.getResult();
      candidatesResult.appendExistingWarning(warnings);
    }
    LOG.debug("\t\t>> Rows=" + rowBlock + "/" + totalRowBlocks + " (Total candidates="
        + candidates.size() + ", previously already processed=" + ignore + ")");
    // now each candidate is given scores
    entity_and_scoreMap = this.disambiguator.constrainedDisambiguate(candidates, table, rowBlock,
        table_cell_col, totalRowBlocks, false);

    entity_and_scoreMap.getWarnings().addAll(warnings);

    return entity_and_scoreMap;
  }

  private boolean checkStablization(final TAnnotation prev_iteration_annotation,
      final TAnnotation table_annotation, final int totalRows,
      final List<Integer> interpreted_columns) {
    // check header annotations
    int columnAnnotationStable = 0;
    boolean stable = false;
    for (final int c : interpreted_columns) {
      final List<TColumnHeaderAnnotation> header_annotations_prev_iteration =
          prev_iteration_annotation.getWinningHeaderAnnotations(c);
      final List<TColumnHeaderAnnotation> header_annotations_current_iteration =
          table_annotation.getWinningHeaderAnnotations(c);
      if (header_annotations_current_iteration.size() == header_annotations_prev_iteration.size()) {
        header_annotations_current_iteration.retainAll(header_annotations_prev_iteration);
        if (header_annotations_current_iteration.size() == header_annotations_prev_iteration
            .size()) {
          columnAnnotationStable++;
        } else {
          return false;
        }
      } else {
        return false;
      }
    }
    if (columnAnnotationStable == interpreted_columns.size()) {
      stable = true;
    }

    // check cell annotations
    final boolean cellAnnotationStable = true;
    for (final int c : interpreted_columns) {
      for (int row = 0; row < totalRows; row++) {
        final List<TCellAnnotation> cell_prev_annotations =
            prev_iteration_annotation.getWinningContentCellAnnotation(row, c);
        final List<TCellAnnotation> cell_current_annotations =
            table_annotation.getWinningContentCellAnnotation(row, c);
        if (cell_current_annotations.size() == cell_prev_annotations.size()) {
          cell_current_annotations.retainAll(cell_prev_annotations);
          if (cell_current_annotations.size() != cell_prev_annotations.size()) {
            return false;
          }
        }
      }
    }
    return stable && cellAnnotationStable;
  }


  private void reviseColumnAndCellAnnotations(final Set<String> allEntityIds, final Table table,
      final TAnnotation currentAnnotation, final List<Integer> interpretedColumns,
      final Constraints constraints) throws STIException {
    // now revise annotations on each of the interpreted columns
    for (final int c : interpretedColumns) {
      LOG.info("\t\t>> for column " + c);
      // sample ranking
      final List<List<Integer>> ranking =
          this.selector.select(table, c, currentAnnotation.getSubjectColumns());

      // get winning header annotation
      final List<TColumnHeaderAnnotation> winningColumnClazzAnnotations =
          currentAnnotation.getWinningHeaderAnnotations(c);
      final Set<String> columnTypes = new HashSet<>();
      for (final TColumnHeaderAnnotation ha : winningColumnClazzAnnotations) {
        columnTypes.add(ha.getAnnotation().getId());
      }

      final Set<Integer> skipRows = constraints.getSkipRowsForColumn(c, table.getNumRows());

      final List<Integer> updated = new ArrayList<>();
      for (int bi = 0; bi < ranking.size(); bi++) {
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

        final TCell sample = table.getContentCell(rows.get(0), c);
        if (sample.getText().length() < 2) {
          LOG.info("\t\t>>> short text cell skipped: " + rows + "," + c + " " + sample.getText());
          continue;
        }

        // constrained disambiguation
        final DisambiguationResult entity_and_scoreMap = disambiguate(allEntityIds, sample, table,
            columnTypes, rows, c, ranking.size(), constraints);

        if (entity_and_scoreMap.getResult().size() > 0) {
          this.disambiguator.addCellAnnotation(table, currentAnnotation, rows, c,
              entity_and_scoreMap);
          updated.addAll(rows);
        } else {
          for (final int row : rows) {
            currentAnnotation.addContentWarnings(row, c, entity_and_scoreMap.getWarnings());
          }
        }
      }


      this.classifier.updateColumnClazz(updated, c, currentAnnotation, table, true);
      // at this point, DC should have been computed. But updateColumnClazz does not add DC to the
      // newly compuetd clazz score.
      // we should add DC to the total score here. however we should use existing DC calculated
      // using the previous annotations,
      // not to recalculate DC using TColumnClassifier.updateClazzScoresByDC
      final TColumnHeaderAnnotation[] columnHeaderAnnotations =
          currentAnnotation.getHeaderAnnotation(c);

      for (final TColumnHeaderAnnotation ha : columnHeaderAnnotations) {
        final Double dc = ha.getScoreElements().get(TMPClazzScorer.SCORE_DOMAIN_CONSENSUS);
        if (dc != null) {
          ha.setFinalScore(ha.getFinalScore() + dc);
        }
      }
      Arrays.sort(columnHeaderAnnotations);
      currentAnnotation.setHeaderAnnotation(c, columnHeaderAnnotations);
    }

  }


  /**
   * start the UPDATE process
   *
   * @param interpretedColumnIndexes
   * @param table
   * @param currentAnnotation
   * @throws ProxyException
   * @throws STIException
   */
  public void update(final List<Integer> interpretedColumnIndexes, final Table table,
      final TAnnotation currentAnnotation) throws STIException {
    update(interpretedColumnIndexes, table, currentAnnotation, new Constraints());
  }


  /**
   * start the UPDATE process
   *
   * @param interpretedColumnIndexes
   * @param table
   * @param currentAnnotation
   * @param constraints
   * @throws ProxyException
   * @throws STIException
   */
  public void update(final List<Integer> interpretedColumnIndexes, final Table table,
      TAnnotation currentAnnotation, final Constraints constraints) throws STIException {

    int currentIteration = 0;
    TAnnotation prevAnnotation;
    // TAnnotation.copy(currentAnnotation, prevAnnotation);
    List<String> domainRep;
    final Set<String> allEntityIds = new HashSet<>();
    boolean stable;
    do {
      ///////////////// solution 2: both prev and current iterations' headers will have dc scores
      ///////////////// added
      LOG.info("\t>> UPDATE begins, iteration:" + currentIteration);
      allEntityIds.addAll(collectAllEntityCandidateIds(table, currentAnnotation));
      // current iteration annotation header scores does not contain dc scores

      // headers will have dc computeElementScores added
      domainRep = createDomainRep(table, currentAnnotation, interpretedColumnIndexes);
      // update clazz scores with dc scores
      this.classifier.updateClazzScoresByDC(currentAnnotation, domainRep, interpretedColumnIndexes);

      prevAnnotation = new TAnnotation(currentAnnotation.getRows(), currentAnnotation.getCols());
      TAnnotation.copy(currentAnnotation, prevAnnotation);

      // scores will be reset, then recalculated. dc scores lost
      reviseColumnAndCellAnnotations(allEntityIds, table, currentAnnotation,
          interpretedColumnIndexes, constraints);
      LOG.info("\t>> update iteration " + currentAnnotation + "complete");
      stable = checkStablization(prevAnnotation, currentAnnotation, table.getNumRows(),
          interpretedColumnIndexes);
      if (!stable) {
        // System.out.println("debug");
      }
      currentIteration++;
    } while (!stable && (currentIteration < STIConstantProperty.UPDATE_PHASE_MAX_ITERATIONS));

    if (currentIteration >= STIConstantProperty.UPDATE_PHASE_MAX_ITERATIONS) {
      LOG.warn("\t>> UPDATE CANNOT STABILIZE AFTER " + currentIteration + " ITERATIONS, Stopped");
      if (prevAnnotation != null) {
        currentAnnotation = new TAnnotation(prevAnnotation.getRows(), prevAnnotation.getCols());
        TAnnotation.copy(prevAnnotation, currentAnnotation);
      }
    } else {
      LOG.info("\t>> UPDATE STABLIZED AFTER " + currentIteration + " ITERATIONS");
    }

  }

}
