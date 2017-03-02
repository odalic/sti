package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.STIEnum;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.feature.OntologyBasedBoWCreator;
import uk.ac.shef.dcs.sti.core.model.RelationColumns;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TCellCellRelationAnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnColumnRelationAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeader;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.TContext;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.scorer.RelationScorer;
import uk.ac.shef.dcs.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.sti.util.CollectionUtils;
import uk.ac.shef.dcs.util.StringUtils;

/**
 *
 */
public class TMPRelationScorer implements RelationScorer {
  public static final String SUM_RE = "sum_re"; // sum of attr match scores
  public static final String SUM_CELL_VOTE = "sum_row_vote";
  public static final String SCORE_RE = "re_score";
  public static final String SCORE_CELL_VOTE = "row_vote";
  public static final String SCORE_CTX_IN_HEADER = "ctx_header_text";
  public static final String SCORE_CTX_IN_COLUMN = "ctx_column_text";
  public static final String SCORE_CTX_OUT = "ctx_out_context";
  public static final String SCORE_DOMAIN_CONSENSUS = "domain_consensus";

  private final Lemmatizer lemmatizer;
  private final List<String> stopWords;
  private final OntologyBasedBoWCreator bowCreator;
  private final double[] wt; // header text, column, title&caption, other

  public TMPRelationScorer(final String nlpResources, final OntologyBasedBoWCreator bowCreator,
      final List<String> stopWords, final double[] wt) throws IOException {
    this.lemmatizer = NLPTools.getInstance(nlpResources).getLemmatizer();
    this.bowCreator = bowCreator;
    this.stopWords = stopWords;
    this.wt = wt;
  }

  @Override
  public List<TColumnColumnRelationAnnotation> computeElementScores(
      final List<TCellCellRelationAnotation> cellcellRelationsOnRow,
      final Collection<TColumnColumnRelationAnnotation> output, final int subjectCol,
      final int objectCol, final Table table) throws STIException {
    List<TColumnColumnRelationAnnotation> candidates;

    candidates = computeREScore(cellcellRelationsOnRow, output, subjectCol, objectCol);
    candidates = computeRCScore(candidates, table, objectCol);

    return candidates;
  }

  @Override
  public Map<String, Double> computeFinal(final TColumnColumnRelationAnnotation relation,
      final int tableRowsTotal) {
    final Map<String, Double> scoreElements = relation.getScoreElements();
    Double sumRE = scoreElements.get(SUM_RE);
    if (sumRE == null) {
      sumRE = 0.0;
    }
    Double sumCellVote = scoreElements.get(SUM_CELL_VOTE);
    if (sumCellVote == null) {
      sumCellVote = 0.0;
    }
    double scoreRE = sumRE / sumCellVote;
    if (sumCellVote == 0.0) {
      scoreRE = 0.0;
    }

    scoreElements.put(SCORE_RE, scoreRE);

    scoreElements.put(SUM_RE, sumRE);

    final double score_vote = sumCellVote / tableRowsTotal;
    scoreElements.put(SCORE_CELL_VOTE, score_vote);

    double base_score = normalize(sumRE, score_vote, tableRowsTotal);

    for (final Map.Entry<String, Double> e : scoreElements.entrySet()) {
      if (e.getKey().equals(SUM_RE) || e.getKey().equals(SUM_CELL_VOTE)
          || e.getKey().equals(SCORE_RE) || e.getKey().equals(SCORE_CELL_VOTE)
          || e.getKey().equals(TColumnColumnRelationAnnotation.SCORE_FINAL)) {
        continue;
      }

      base_score += e.getValue();
    }
    scoreElements.put(TColumnHeaderAnnotation.SCORE_FINAL, base_score);
    relation.setFinalScore(base_score);
    return scoreElements;
  }

  /**
   * compute relation context score
   * <p>
   * context scores are only computed once. The code will check if they already edist for each
   * TColumnColumnRelationAnnotation and if so, it will not recompute it.
   *
   * @param candidates
   * @param table
   * @param column
   * @return
   */
  @Override
  public List<TColumnColumnRelationAnnotation> computeRCScore(
      final Collection<TColumnColumnRelationAnnotation> candidates, final Table table,
      final int column) throws STIException {
    Set<String> bowHeader = null;
    List<String> bowColumn = null, bowOutTableImportantCtx = null, bowOutTableTrivialCtx = null;
    for (final TColumnColumnRelationAnnotation ccRelationAnnotation : candidates) {
      final Double scoreCtxHeaderText =
          ccRelationAnnotation.getScoreElements().get(SCORE_CTX_IN_HEADER);
      final Double scoreCtxColumnText =
          ccRelationAnnotation.getScoreElements().get(SCORE_CTX_IN_COLUMN);
      final Double scoreCtxTableContext =
          ccRelationAnnotation.getScoreElements().get(SCORE_CTX_OUT);

      if ((scoreCtxColumnText != null) && (scoreCtxHeaderText != null)
          && (scoreCtxTableContext != null)) {
        continue;
      }

      final Set<String> relationBOW = createRelationBOW(ccRelationAnnotation, true,
          STIConstantProperty.BOW_DISCARD_SINGLE_CHAR);

      if (scoreCtxHeaderText == null) {
        bowHeader = createHeaderTextBOW(bowHeader, table, column);
        final double ctxScoreHeaderText =
            CollectionUtils.computeFrequencyWeightedDice(relationBOW, bowHeader) * this.wt[0];
        ccRelationAnnotation.getScoreElements().put(SCORE_CTX_IN_HEADER, ctxScoreHeaderText);
      }

      if (scoreCtxColumnText == null) {
        bowColumn = createColumnBOW(bowColumn, table, column);
        final double ctx_column =
            CollectionUtils.computeFrequencyWeightedDice(relationBOW, bowColumn) * this.wt[1];
        ccRelationAnnotation.getScoreElements().put(SCORE_CTX_IN_COLUMN, ctx_column);
      }

      if (scoreCtxTableContext == null) {
        bowOutTableImportantCtx = createImportantOutTableCtxBOW(bowOutTableImportantCtx, table);
        final double ctx_out_important =
            CollectionUtils.computeFrequencyWeightedDice(relationBOW, bowOutTableImportantCtx)
                * this.wt[2];
        bowOutTableTrivialCtx = createOutTableCtx(bowOutTableTrivialCtx, table);
        final double ctx_out_trivial =
            CollectionUtils.computeFrequencyWeightedDice(relationBOW, bowOutTableTrivialCtx)
                * this.wt[3];
        ccRelationAnnotation.getScoreElements().put(SCORE_CTX_OUT,
            ctx_out_important + ctx_out_trivial);
      }

    }

    return new ArrayList<>(candidates);
  }

  /**
   * Compute relation instance score
   *
   * @param cellcellRelationAnotations
   * @param output
   * @param subjectCol
   * @param objectCol
   * @return
   */
  @Override
  public List<TColumnColumnRelationAnnotation> computeREScore(
      final List<TCellCellRelationAnotation> cellcellRelationAnotations,
      final Collection<TColumnColumnRelationAnnotation> output, final int subjectCol,
      final int objectCol) throws STIException {

    // for this row
    TCellCellRelationAnotation winningAnnotation = null;
    double winningScore = 0.0;
    for (final TCellCellRelationAnotation cellcellRelationAnnotation : cellcellRelationAnotations) { // each
                                                                                                     // candidate
                                                                                                     // entity
                                                                                                     // in
                                                                                                     // this
                                                                                                     // cell
      final double attrMatchScore = cellcellRelationAnnotation.getWinningAttributeMatchScore();
      if (attrMatchScore > winningScore) {
        winningScore = attrMatchScore;
        winningAnnotation = cellcellRelationAnnotation;
      }
    }
    if ((cellcellRelationAnotations.size() == 0) || (winningAnnotation == null)) {
      return new ArrayList<>(output);
    }

    Collections.sort(cellcellRelationAnotations);

    // consolidate scores from this cell
    for (final TCellCellRelationAnotation cellcellRelationAnnotation : cellcellRelationAnotations) {
      if (cellcellRelationAnnotation.getWinningAttributeMatchScore() < winningScore) {
        break;
      }

      TColumnColumnRelationAnnotation columncolumnRelationAnnotation = null;
      for (final TColumnColumnRelationAnnotation key : output) {
        if (key.getRelationURI().equals(cellcellRelationAnnotation.getRelationURI())) {
          columncolumnRelationAnnotation = key;
          break;
        }
      }
      if (columncolumnRelationAnnotation == null) {
        columncolumnRelationAnnotation = new TColumnColumnRelationAnnotation(
            new RelationColumns(subjectCol, objectCol), cellcellRelationAnnotation.getRelationURI(),
            cellcellRelationAnnotation.getRelationLabel(), 0.0);
        output.add(columncolumnRelationAnnotation);
      }

      Map<String, Double> scoreElements = columncolumnRelationAnnotation.getScoreElements();
      if ((scoreElements == null) || (scoreElements.size() == 0)) {
        scoreElements = new HashMap<>();
        scoreElements.put(SUM_RE, 0.0);
        scoreElements.put(SUM_CELL_VOTE, 0.0);
      }
      Double sumRE = scoreElements.get(SUM_RE);
      if (sumRE == null) {
        sumRE = 0.0;
      }
      scoreElements.put(SUM_RE, sumRE + winningScore);
      Double sumCellVote = scoreElements.get(SUM_CELL_VOTE);
      if (sumCellVote == null) {
        sumCellVote = 0.0;
      }
      scoreElements.put(SUM_CELL_VOTE, sumCellVote + 1.0);
      columncolumnRelationAnnotation.setScoreElements(scoreElements);

      // output.add(columncolumnRelationAnnotation);
    }

    return new ArrayList<>(output);
  }

  private List<String> createColumnBOW(final List<String> bag_of_words_for_column,
      final Table table, final int column) {
    if (bag_of_words_for_column != null) {
      return bag_of_words_for_column;
    }
    final List<String> bow = new ArrayList<>();
    for (int row = 0; row < table.getNumRows(); row++) {
      final TCell tcc = table.getContentCell(row, column);
      if (tcc.getText() != null) {
        bow.addAll(this.lemmatizer.lemmatize(StringUtils.toBagOfWords(tcc.getText(), true, true,
            STIConstantProperty.BOW_DISCARD_SINGLE_CHAR)));
      }
    }
    bow.removeAll(this.stopWords);
    return bow;
  }

  private Set<String> createHeaderTextBOW(final Set<String> bag_of_words_for_header,
      final Table table, final int column) {
    if (bag_of_words_for_header != null) {
      return bag_of_words_for_header;
    }
    final Set<String> bow = new HashSet<>();
    final TColumnHeader header = table.getColumnHeader(column);
    if ((header != null) && (header.getHeaderText() != null)
        && !header.getHeaderText().equals(STIEnum.TABLE_HEADER_UNKNOWN.getValue())) {
      bow.addAll(this.lemmatizer.lemmatize(StringUtils.toBagOfWords(header.getHeaderText(), true,
          true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR)));
    }
    bow.removeAll(STIConstantProperty.FUNCTIONAL_STOPWORDS);
    // also remove special, generic words, like "title", "name"
    bow.remove("title");
    bow.remove("name");
    return bow;
  }

  private List<String> createImportantOutTableCtxBOW(
      final List<String> bag_of_words_for_table_context, final Table table) {
    if (bag_of_words_for_table_context != null) {
      return bag_of_words_for_table_context;
    }
    if (table.getContexts() == null) {
      return new ArrayList<>();
    }

    final List<String> bow = new ArrayList<>();
    for (int i = 0; i < table.getContexts().size(); i++) {
      final TContext tx = table.getContexts().get(i);
      if (tx.getType().equals(TContext.TableContextType.PAGETITLE)
          || tx.getType().equals(TContext.TableContextType.CAPTION)) {
        bow.addAll(this.lemmatizer.lemmatize(StringUtils.toBagOfWords(tx.getText(), true, true,
            STIConstantProperty.BOW_DISCARD_SINGLE_CHAR)));
      }
    }
    bow.removeAll(this.stopWords);
    return bow;
  }

  private List<String> createOutTableCtx(final List<String> bag_of_words_for_table_context,
      final Table table) {
    if (bag_of_words_for_table_context != null) {
      return bag_of_words_for_table_context;
    }
    if (table.getContexts() == null) {
      return new ArrayList<>();
    }

    final List<String> bow = new ArrayList<String>();
    for (int i = 0; i < table.getContexts().size(); i++) {
      final TContext tx = table.getContexts().get(i);
      if (!tx.getType().equals(TContext.TableContextType.PAGETITLE)
          && !tx.getType().equals(TContext.TableContextType.CAPTION)) {
        bow.addAll(this.lemmatizer.lemmatize(StringUtils.toBagOfWords(tx.getText(), true, true,
            STIConstantProperty.BOW_DISCARD_SINGLE_CHAR)));
      }
    }
    bow.removeAll(this.stopWords);
    return bow;
  }


  private Set<String> createRelationBOW(final TColumnColumnRelationAnnotation relation,
      final boolean lowercase, final boolean discard_single_char) {
    final Set<String> bow = new HashSet<>();
    bow.addAll(this.bowCreator.create(relation.getRelationURI()));
    bow.addAll(StringUtils.toBagOfWords(relation.getRelationLabel(), lowercase, true,
        discard_single_char));
    bow.removeAll(STIConstantProperty.FUNCTIONAL_STOPWORDS);
    return bow;
  }

  private double normalize(final double sum_cbr_match, final double sum_cbr_vote,
      final double total_table_rows) {
    if (sum_cbr_vote == 0) {
      return 0.0;
    }

    return sum_cbr_match / total_table_rows; // this is equivalent to below

    /*
     * double score_cbr_vote = sum_cbr_vote / total_table_rows; double base_score = score_cbr_vote *
     * (sum_cbr_match / sum_cbr_vote); return base_score;
     */
  }

  @Override
  public double scoreDC(final TColumnColumnRelationAnnotation hbr,
      final List<String> domain_representation) throws STIException {
    final Set<String> annotation_bow =
        createRelationBOW(hbr, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR);
    // annotation_bow.removeAll(TableMinerConstants.FUNCTIONAL_STOPWORDS);
    double score =
        CollectionUtils.computeFrequencyWeightedDice(annotation_bow, domain_representation);
    score = Math.sqrt(score);
    hbr.getScoreElements().put(SCORE_DOMAIN_CONSENSUS, score);

    return score; // To change body of implemented methods use File | Settings | File Templates.
  }
}
