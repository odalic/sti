package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.kbproxy.Proxy;
import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.STIEnum;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.feature.OntologyBasedBoWCreator;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeader;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.TContext;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.scorer.ClazzScorer;
import uk.ac.shef.dcs.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.sti.util.CollectionUtils;
import uk.ac.shef.dcs.util.Pair;
import uk.ac.shef.dcs.util.StringUtils;

/**
 * Firstly, computeElementScores each candidate type for this column, based on 1) # of candidate
 * entities lead to that type 2) candidate entity's disamb computeElementScores Then once type is
 * decided for this column, re-computeElementScores disambiguation scores for every candidate entity
 */
public class TMPClazzScorer implements ClazzScorer {

  public static final String SUM_CE = "sum_ce";
  public static final String SUM_CELL_VOTE = "sum_cell_vote";
  public static final String SCORE_CE = "ce_score";
  public static final String SCORE_CELL_VOTE = "cell_vote";
  public static final String SCORE_CTX_IN_HEADER = "ctx_header_text";
  public static final String SCORE_CTX_IN_COLUMN = "ctx_column_text";
  public static final String SCORE_CTX_OUT = "ctx_out_context";
  public static final String SCORE_DOMAIN_CONSENSUS = "domain_consensus";

  public static final String SCORE_FORMER = "former_score";
  public static final String SCORE_CLASS_HIERARCHY = "hierarchy_score";

  private static final Logger LOG = LoggerFactory.getLogger(TMPClazzScorer.class.getName());

  protected Lemmatizer lemmatizer;
  protected List<String> stopWords;
  protected OntologyBasedBoWCreator bowCreator;
  private final double[] wt; // header text, column, out table ctx: title&caption, out table
                             // ctx:other

  /**
   * @key - class from KB
   * @value - depth of class in hierarchy
   */
  private Map<String, Integer> depths;

  public TMPClazzScorer(final String nlpResources, final OntologyBasedBoWCreator bowCreator,
      final List<String> stopWords, final double[] weights) throws IOException {
    this.lemmatizer = NLPTools.getInstance(nlpResources).getLemmatizer();
    this.bowCreator = bowCreator;
    this.stopWords = stopWords;
    this.wt = weights;

    this.depths = new HashMap<>();
  }

  /**
   * compute concept context computeElementScores
   *
   * @param candidates
   * @param table
   * @param column
   * @return
   */
  @Override
  public List<TColumnHeaderAnnotation> computeCCScore(
      final Collection<TColumnHeaderAnnotation> candidates, final Table table, final int column) {
    List<String> bowHeader = null, bowColumn = null, bowImportantContext = null,
        bowTrivialContext = null;
    for (final TColumnHeaderAnnotation ha : candidates) {
      final Double scoreCtxHeader = ha.getScoreElements().get(SCORE_CTX_IN_HEADER);
      final Double scoreCtxColumn = ha.getScoreElements().get(SCORE_CTX_IN_COLUMN);
      final Double scoreCtxOutTable = ha.getScoreElements().get(SCORE_CTX_OUT);

      if ((scoreCtxColumn != null) && (scoreCtxHeader != null) && (scoreCtxOutTable != null)) {
        continue;
      }

      final Set<String> clazzBOW = new HashSet<>(createClazzBOW(ha, true,
          STIConstantProperty.BOW_DISCARD_SINGLE_CHAR, STIConstantProperty.BOW_CLAZZ_INCLUDE_URI));

      if (scoreCtxHeader == null) {
        bowHeader = createHeaderTextBOW(bowHeader, table, column);
        final double ctx_header_text =
            CollectionUtils.computeFrequencyWeightedDice(clazzBOW, bowHeader) * this.wt[0];
        ha.getScoreElements().put(SCORE_CTX_IN_HEADER, ctx_header_text);
      }

      if (scoreCtxColumn == null) {
        bowColumn = createColumnBOW(bowColumn, table, column);
        final double ctx_column =
            CollectionUtils.computeFrequencyWeightedDice(clazzBOW, bowColumn) * this.wt[1];
        // CollectionUtils.computeCoverage(bag_of_words_for_column, new
        // ArrayList<String>(annotation_bow)) * weights[1];
        ha.getScoreElements().put(SCORE_CTX_IN_COLUMN, ctx_column);
      }

      if (scoreCtxOutTable == null) {
        bowImportantContext = createImportantOutTableCtxBOW(bowImportantContext, table);
        final double ctx_table_major =
            CollectionUtils.computeFrequencyWeightedDice(clazzBOW, bowImportantContext)
                * this.wt[2];
        // CollectionUtils.computeCoverage(bag_of_words_for_table_major_context, new
        // ArrayList<String>(annotation_bow)) * weights[3];
        bowTrivialContext = createTrivialOutTableCtxBOW(bowTrivialContext, table);
        final double ctx_table_other =
            CollectionUtils.computeFrequencyWeightedDice(clazzBOW, bowTrivialContext) * this.wt[3];
        // CollectionUtils.computeCoverage(bag_of_words_for_table_other_context, new
        // ArrayList<String>(annotation_bow)) * weights[2];
        ha.getScoreElements().put(SCORE_CTX_OUT, ctx_table_major + ctx_table_other);
      }

    }

    if (candidates instanceof List) {
      return (List<TColumnHeaderAnnotation>) candidates;
    } else {
      return new ArrayList<>(candidates);
    }
  }


  /**
   * compute concept instance computeElementScores
   *
   * @param entities
   * @param existingHeaderAnnotations
   * @param table
   * @param row
   * @param column
   * @return
   */
  @Override
  public List<TColumnHeaderAnnotation> computeCEScore(
      final List<Pair<Entity, Map<String, Double>>> entities,
      final Collection<TColumnHeaderAnnotation> existingHeaderAnnotations, final Table table,
      final int row, final int column) {
    final List<TColumnHeaderAnnotation> updatedHeaderAnnotations =
        new ArrayList<>(existingHeaderAnnotations);

    // for this row
    Entity winningEntity = null;
    double highestScore = 0.0;
    for (final Pair<Entity, Map<String, Double>> es : entities) { // each candidate entity in this
                                                                  // cell
      final Entity entity = es.getKey();
      // each assigned type receives a computeElementScores of 1, and the bonus computeElementScores
      // due to disambiguation result
      final double entityCFScore = es.getValue().get(TCellAnnotation.SCORE_FINAL);
      if (entityCFScore > highestScore) {
        highestScore = entityCFScore;
        winningEntity = entity;
      }
    }
    if ((entities.size() == 0) || (winningEntity == null)) {
      // this entity has a computeElementScores of 0.0, it should not contribute to the header
      // typing, but we may still keep it as candidate for this cell
      LOG.warn("no clazz elected by cell: (" + row + "," + column + ")");
      return updatedHeaderAnnotations;
    }


    for (final Pair<Entity, Map<String, Double>> es : entities) {
      final Entity entity = es.getKey();
      final double entityCFScore = es.getValue().get(TCellAnnotation.SCORE_FINAL);
      if (entityCFScore != highestScore) {
        continue;
      }

      final Set<String> votedClazzByThisCell = new HashSet<>(); // each type will receive a max of 1
                                                                // vote from each cell. If multiple
                                                                // candidates have the same highest
                                                                // computeElementScores and casts
                                                                // same votes, they are counted oly
                                                                // once
      final List<Clazz> votedClazzByThisEntity = entity.getTypes();

      // consolidate scores from this cell
      for (final Clazz clazz : votedClazzByThisEntity) {
        if (votedClazzByThisCell.contains(clazz.getId())) {
          continue;
        }

        votedClazzByThisCell.add(clazz.getId());

        final String headerText = table.getColumnHeader(column).getHeaderText();

        // is this clazz (of the winning entity) already put into the collection of header
        // annotations?
        TColumnHeaderAnnotation hAnnotation = null;
        for (final TColumnHeaderAnnotation headerAnnotation : updatedHeaderAnnotations) {
          if (headerAnnotation.getHeaderText().equals(headerText)
              && headerAnnotation.getAnnotation().equals(clazz)) {
            hAnnotation = headerAnnotation;
            break;
          }
        }
        if (hAnnotation == null) {
          hAnnotation = new TColumnHeaderAnnotation(headerText, clazz, 0.0);
        }
        Map<String, Double> scoreElements = hAnnotation.getScoreElements();
        if ((scoreElements == null) || (scoreElements.size() == 0)) {
          scoreElements = new HashMap<>();
          scoreElements.put(SUM_CE, 0.0);
          scoreElements.put(SUM_CELL_VOTE, 0.0);
        }
        Double sumCE = scoreElements.get(SUM_CE);
        if (sumCE == null) {
          sumCE = 0.0;
        }
        scoreElements.put(SUM_CE, sumCE + highestScore);
        Double sumCellVote = scoreElements.get(SUM_CELL_VOTE);
        if (sumCellVote == null) {
          sumCellVote = 0.0;
        }
        scoreElements.put(SUM_CELL_VOTE, sumCellVote + 1.0);
        hAnnotation.setScoreElements(scoreElements);

        if (!updatedHeaderAnnotations.contains(hAnnotation)) {
          updatedHeaderAnnotations.add(hAnnotation);
        }
      }
    }

    return updatedHeaderAnnotations;
  }


  /**
   * compute domain concensus
   *
   * @param ha
   * @param domain_representation
   * @return
   */
  @Override
  public double computeDC(final TColumnHeaderAnnotation ha,
      final List<String> domain_representation) throws STIException {
    final List<String> annotation_bow = createClazzBOW(ha, true,
        STIConstantProperty.BOW_DISCARD_SINGLE_CHAR, STIConstantProperty.BOW_CLAZZ_INCLUDE_URI);
    double score =
        CollectionUtils.computeFrequencyWeightedDice(annotation_bow, domain_representation);
    score = Math.sqrt(score) * 2;
    ha.getScoreElements().put(SCORE_DOMAIN_CONSENSUS, score);

    return score; // To change body of implemented methods use File | Settings | File Templates.
  }


  /*
   * public Map<String, Double> computeFinal(TColumnHeaderAnnotation ha, int tableRowsTotal) {
   * Map<String, Double> scoreElements = ha.getScoreElements(); double sum = 0.0; double
   * score_entity_disamb = scoreElements.get(TColumnHeaderAnnotation.SUM_CE);
   *
   * scoreElements.put(TColumnHeaderAnnotation.SCORE_CE, score_entity_disamb);
   *
   * double score_entity_vote =
   * scoreElements.get(TColumnHeaderAnnotation.SUM_ENTITY_VOTE)/(double)tableRowsTotal;
   * scoreElements.put(TColumnHeaderAnnotation.SCORE_ENTITY_VOTE, score_entity_vote);
   *
   * for (Map.Entry<String, Double> e : scoreElements.entrySet()) { if
   * (e.getKey().equals(TColumnHeaderAnnotation.SUM_CE) ||
   * e.getKey().equals(TColumnHeaderAnnotation.SUM_ENTITY_VOTE) ||
   * e.getKey().equals(TColumnHeaderAnnotation.FINAL)) continue;
   *
   * sum += e.getValue(); } scoreElements.put(TColumnHeaderAnnotation.FINAL, sum);
   * ha.setFinalScore(sum); return scoreElements; }
   */

  @Override
  public List<TColumnHeaderAnnotation> computeElementScores(
      final List<Pair<Entity, Map<String, Double>>> input,
      final Collection<TColumnHeaderAnnotation> headerAnnotationCandidates, final Table table,
      final List<Integer> rows, final int column,
      final Proxy kbProxy) {
    List<TColumnHeaderAnnotation> candidates = new ArrayList<>();
    for (final int row : rows) {
      candidates = computeCEScore(input, headerAnnotationCandidates, table, row, column);
    }
    candidates = computeCCScore(candidates, table, column);

//    candidates = computeHierarchyScore(candidates, kbProxy);

    return candidates;
  }

  @Override
  public Map<String, Double> computeFinal(final TColumnHeaderAnnotation ha,
      final int tableRowsTotal) {
    final Map<String, Double> scoreElements = ha.getScoreElements();
    Double sum_ce = scoreElements.get(SUM_CE);
    if (sum_ce == null) {
      sum_ce = 0.0;
    }
    Double sum_entity_vote = scoreElements.get(SUM_CELL_VOTE);
    if (sum_entity_vote == null) {
      sum_entity_vote = 0.0;
    }

    double ce = normalize(sum_ce, sum_entity_vote, tableRowsTotal); // sum_entity_vote==0?0:sum_ce /
                                                                    // tableRowsTotal;
    scoreElements.put(SCORE_CE, ce);

    final double score_entity_vote = sum_entity_vote / tableRowsTotal;
    scoreElements.put(SCORE_CELL_VOTE, score_entity_vote);

    for (final Map.Entry<String, Double> e : scoreElements.entrySet()) {
      if (e.getKey().startsWith("ctx")) {
        ce += e.getValue();
      }
    }

    scoreElements.put(SCORE_FORMER, ce);

    Double score_hierarchy = scoreElements.get(SCORE_CLASS_HIERARCHY);
    if (score_hierarchy == null) {
      score_hierarchy = 0.0;
    }
    ce += score_hierarchy;

    scoreElements.put(TColumnHeaderAnnotation.SCORE_FINAL, ce);
    ha.setFinalScore(ce);
    return scoreElements;
  }

  protected List<String> createClazzBOW(final TColumnHeaderAnnotation ha, final boolean lowercase,
      final boolean discard_single_char, final boolean include_url) {
    final List<String> bow = new ArrayList<>();
    if (include_url) {
      bow.addAll(this.bowCreator.create(ha.getAnnotation().getId()));
    }

    String label = StringUtils.toAlphaNumericWhitechar(ha.getAnnotation().getLabel()).trim();
    if (lowercase) {
      label = label.toLowerCase();
    }
    for (String s : label.split("\\s+")) {
      s = s.trim();
      if (s.length() > 0) {
        bow.add(s);
      }
    }

    if (discard_single_char) {
      final Iterator<String> it = bow.iterator();
      while (it.hasNext()) {
        final String t = it.next();
        if (t.length() < 2) {
          it.remove();
        }
      }
    }
    bow.removeAll(STIConstantProperty.FUNCTIONAL_STOPWORDS);
    return bow;
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

  private List<String> createHeaderTextBOW(final List<String> bowHeaderText, final Table table,
      final int column) {
    if (bowHeaderText != null) {
      return bowHeaderText;
    }
    final List<String> bow = new ArrayList<>();

    // for (int c = 0; c < table.getNumCols(); c++) {
    final TColumnHeader header = table.getColumnHeader(column);
    if ((header != null) && (header.getHeaderText() != null)
        && !header.getHeaderText().equals(STIEnum.TABLE_HEADER_UNKNOWN.getValue())) {
      bow.addAll(this.lemmatizer.lemmatize(StringUtils.toBagOfWords(header.getHeaderText(), true,
          true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR)));
    }
    // }
    bow.removeAll(STIConstantProperty.FUNCTIONAL_STOPWORDS);
    // also remove special, generic words, like "title", "name"
    bow.remove("title");
    bow.remove("name");
    return bow;
  }

  private List<String> createImportantOutTableCtxBOW(final List<String> bowOutTableCtx,
      final Table table) {
    if (bowOutTableCtx != null) {
      return bowOutTableCtx;
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

  private List<String> createTrivialOutTableCtxBOW(final List<String> bowOutTableCtx,
      final Table table) {
    if (bowOutTableCtx != null) {
      return bowOutTableCtx;
    }
    if (table.getContexts() == null) {
      return new ArrayList<>();
    }

    final List<String> bow = new ArrayList<>();
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


  private double normalize(final double sum_ce, final double sum_entity_vote,
      final double total_table_rows) {
    if (sum_entity_vote == 0) {
      return 0.0;
    }

    return sum_ce / total_table_rows; // this is equivalent to the below

    /*
     * double score_entity_vote = sum_entity_vote / total_table_rows; double base_score =
     * score_entity_vote * (sum_ce / sum_entity_vote); return base_score;
     */
  }

  @Override
  public List<TColumnHeaderAnnotation> computeHierarchyScore(
      final Collection<TColumnHeaderAnnotation> candidates, final Proxy kbProxy) {
    for (final TColumnHeaderAnnotation ha : candidates) {
      final Double scoreClassHierarchy = ha.getScoreElements().get(SCORE_CLASS_HIERARCHY);

      if (scoreClassHierarchy == null) {
        final double score_hierarchy = 0.02 * findDepthOfClazz(ha.getAnnotation().getId(), kbProxy);
        ha.getScoreElements().put(SCORE_CLASS_HIERARCHY, score_hierarchy);
      }
    }

    if (candidates instanceof List) {
      return (List<TColumnHeaderAnnotation>) candidates;
    } else {
      return new ArrayList<>(candidates);
    }
  }

  private int findDepthOfClazz(final String clazz, final Proxy kbProxy) {
    if (!depths.containsKey(clazz)) {
      fetchDepthsForClasses(clazz, kbProxy);
    }

    return (depths.get(clazz) < 0) ? 0 : depths.get(clazz);
  }

  private void fetchDepthsForClasses(String clazz, Proxy kbProxy) {
    List<String> classChain = new ArrayList<>();
    Integer lastDepth = null;

    while (clazz != null) {
      if (depths.containsKey(clazz)) {
        // parent's depth is fetched
        lastDepth = depths.get(clazz);
        break;
      }

      if (classChain.contains(clazz)) {
        // cycle detected in ontology chain
        lastDepth = -1;
        break;
      }
      classChain.add(clazz);

      clazz = kbProxy.findParentClazz(clazz).getResult();
    }

    if (lastDepth == null) {
      // add new chain
      int itemDepth = classChain.size() - 1;
      for (String chainItem : classChain) {
        depths.put(chainItem, itemDepth);
        itemDepth--;
      }
    }
    else if (lastDepth < 0) {
      // cycle
      for (String chainItem : classChain) {
        depths.put(chainItem, lastDepth);
      }
    }
    else {
      // add to existing chain
      int itemDepth = classChain.size() + lastDepth;
      for (String chainItem : classChain) {
        depths.put(chainItem, itemDepth);
        itemDepth--;
      }
    }
  }
}
