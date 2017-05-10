package uk.ac.shef.dcs.sti.core.subjectcol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.colt.matrix.DoubleMatrix2D;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentRowRanker;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.stopping.StoppingCriteriaInstantiator;
import uk.ac.shef.dcs.sti.core.extension.positions.ColumnPosition;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.util.Cache;
import uk.ac.shef.dcs.util.Pair;
import uk.ac.shef.dcs.websearch.WebSearchException;

/**
 * This class implements a decision tree logic to infer among all columns in a table, which ONE is
 * likely the main entity column
 */
public class SubjectColumnDetector {

  private static Logger LOG = LoggerFactory.getLogger(SubjectColumnDetector.class.getName());
  private final TColumnFeatureGenerator featureGenerator;
  private final TContentRowRanker tRowRanker;
  private final String stoppingCriteriaClassname;
  private final String[] stoppingCriteriaParams;
  private final boolean useWS;


  public SubjectColumnDetector(final TContentRowRanker tRowRanker,
      final String stoppingCriteriaClassname, final String[] stoppingCriteriaParams,
      final Cache cache, final String nlpResource, final boolean useWS,
      final List<String> stopwords, final String webSearchPropFile)
      throws IOException, WebSearchException {
    this.featureGenerator =
        new TColumnFeatureGenerator(cache, nlpResource, stopwords, webSearchPropFile);
    this.tRowRanker = tRowRanker;
    this.stoppingCriteriaClassname = stoppingCriteriaClassname;
    this.stoppingCriteriaParams = stoppingCriteriaParams;
    this.useWS = useWS;
  }

  private void attachColumnFeature(final Table table,
      final List<TColumnFeature> featuresOfAllColumns) {
    for (final TColumnFeature cf : featuresOfAllColumns) {
      table.getColumnHeader(cf.getColId()).setFeature(cf);
    }
  }

  /**
   * The decision tree logic is: 1. If col is the only NE likely col in the table, choose the column
   * 2. If col is NE likely, and it is the only one having non-empty cells, choose the column
   *
   * @param table
   * @return a list of Pair objects, where first object is the column index; second is a pair where
   *         the first part is the computeElementScores probability that asserts that column being
   *         the main column of the table, the second part is a boolean indicating whether the
   *         column is acronym column. (only NE likely columns can be considered main column)
   */
  public List<Pair<Integer, Pair<Double, Boolean>>> compute(final Table table,
      final ColumnPosition suggestedSubject, final int... skipColumns)
      throws IOException, ClassNotFoundException {
    final List<Pair<Integer, Pair<Double, Boolean>>> rs = new ArrayList<>();

    // 1. initiate all columns' feature objects
    final List<TColumnFeature> featuresOfAllColumns = new ArrayList<>(table.getNumCols());
    for (int c = 0; c < table.getNumCols(); c++) {
      boolean skip = false;
      for (final int i : skipColumns) {
        if (c == i) {
          skip = true;
          break;
        }
      }
      if (!skip) {
        featuresOfAllColumns.add(new TColumnFeature(c, table.getNumRows()));
      }
    }

    // 2. infer column datatype
    TColumnFeatureGenerator.setColumnDataTypes(table);

    // 3. infer the most frequent datatype,
    this.featureGenerator.setMostFrequentDataTypes(featuresOfAllColumns, table);

    // 3.5. (added): is the subject column position suggested by the user?
    if ((suggestedSubject != null) && (suggestedSubject.getIndex() < table.getNumCols())) {
      final Pair<Integer, Pair<Double, Boolean>> oo =
          new Pair<>(suggestedSubject.getIndex(), new Pair<>(1.0, false));
      rs.add(oo);
      attachColumnFeature(table, featuresOfAllColumns);
      return rs;
    }

    // 4. select only NE columns to further learn
    final List<TColumnFeature> featuresOfNEColumns =
        selectOnlyNEColumnFeatures(featuresOfAllColumns);
    if (featuresOfNEColumns.size() == 0) {
      LOG.warn("This table does not contain columns that are likely to contain named entities.");
      final Pair<Integer, Pair<Double, Boolean>> oo = new Pair<>(0, new Pair<>(1.0, false));
      rs.add(oo);
      attachColumnFeature(table, featuresOfAllColumns);
      return rs;
    }

    this.featureGenerator.setEmptyCellCount(featuresOfNEColumns, table); // warning:always count
                                                                         // empty cells first!!!!!
    this.featureGenerator.setUniqueValueCount(featuresOfNEColumns, table);
    this.featureGenerator.setAcronymColumnBoolean(featuresOfNEColumns, table); // warning: this must
                                                                               // be run after
                                                                               // counting empty
                                                                               // cells!!!

    // 5. is any NE column the only valid NE column in the table?
    final int onlyNECol = this.featureGenerator.setOnlyNEColumn(featuresOfNEColumns);
    // 5 - yes:
    if (onlyNECol != -1) {
      final Pair<Integer, Pair<Double, Boolean>> oo = new Pair<>(onlyNECol, new Pair<>(1.0, false));
      rs.add(oo);
      for (final TColumnFeature cf : featuresOfAllColumns) {
        table.getColumnHeader(cf.getColId()).setFeature(cf);
      }
      return rs;
    }

    // 6. is any NE column the only one that has no empty cells?
    final int onlyNonEmptyNECol =
        this.featureGenerator.setOnlyNonEmptyNEColumn(featuresOfNEColumns);
    if (onlyNonEmptyNECol != -1) {
      final Pair<Integer, Pair<Double, Boolean>> oo =
          new Pair<>(onlyNonEmptyNECol, new Pair<>(1.0, false));
      rs.add(oo);
      attachColumnFeature(table, featuresOfAllColumns);
      return rs;
    }

    // 7. is any NE column the only one that has non-duplicate values on every row
    // and that it is NOT an acronym column?
    this.featureGenerator.setOnlyNonDuplicateNEColumn(featuresOfNEColumns, table);
    
    // todo: test this. original has the following block, which has no effect on results
    /*
     * if (onlyNonDuplicateNECol != -1) {
     *
     * Pair<Integer, Pair<Double, Boolean>> oo = new Pair<>( onlyNonDuplicateNECol, new Pair<>(1.0,
     * false) ); // rs.add(oo); for (TColumnFeature cf : featuresOfAllColumns) {
     * table.getColumnHeader(cf.getColId()).setFeature(cf); } // return rs; }
     */

    // 7.5 ====== this is a dangerous rule as it MAY overdo (have not checked thou) true positives
    // ======
    final List<Integer> ignoreColumns = new ArrayList<>();
    this.featureGenerator.setInvalidHeaderTextSyntax(featuresOfNEColumns, table);
    for (final TColumnFeature cf : featuresOfNEColumns) {
      if (cf.isInvalidPOS()) {
        ignoreColumns.add(cf.getColId());
      }
    }
    // if columns to be ignored due to invalid header text is less than total columns
    // to be considered,we can isValidAttribute them
    // otherwise, if we are told all columns should be ignored, dont isValidAttribute any candidate
    // ne columns
    if ((ignoreColumns.size() > 0) && (ignoreColumns.size() != featuresOfNEColumns.size())) {
      final Iterator<TColumnFeature> it = featuresOfNEColumns.iterator();
      while (it.hasNext()) {
        final TColumnFeature cf = it.next();
        if (cf.isInvalidPOS()) {
          it.remove();
        }
      }
    }
    if (featuresOfNEColumns.size() == 1) {
      final Pair<Integer, Pair<Double, Boolean>> oo =
          new Pair<>(featuresOfNEColumns.get(0).getColId(), new Pair<>(1.0, false));
      rs.add(oo);
      attachColumnFeature(table, featuresOfAllColumns);
      return rs;
    }

    // 8. generate feature - 1st NE column
    this.featureGenerator.setIsFirstNEColumn(featuresOfNEColumns);

    // 9. generate features - context computeElementScores
    LOG.debug("Computing cm computeElementScores"); // todo more testing required for this
    this.featureGenerator.setCMScores(featuresOfNEColumns, table);

    // 10. generate features - web search matcher
    if (this.useWS) {
      computeWSScores(table, featuresOfNEColumns);
    }

    // added: reset all scores to use relative scoring
    normalizeScores(featuresOfNEColumns);

    // 12. then let's perform reasoning based on the remaining features:
    // diversity computeElementScores; 1st ne column; context computeElementScores; web search
    // computeElementScores
    final Map<Integer, Pair<Double, Boolean>> finalScores =
        new SubjectColumnScorerHeuristic().score(featuresOfNEColumns);
    final List<Integer> candidates = new ArrayList<>(finalScores.keySet());
    // tiebreaker_reset(allNEColumnCandidates, inferenceScores);

    Collections.sort(candidates,
        (o1, o2) -> finalScores.get(o2).getKey().compareTo(finalScores.get(o1).getKey()));

    for (final int ci : candidates) {
      final Pair<Integer, Pair<Double, Boolean>> oo = new Pair<>(ci, finalScores.get(ci));
      rs.add(oo);
    }

    for (final TColumnFeature cf : featuresOfAllColumns) {
      table.getColumnHeader(cf.getColId()).setFeature(cf);
    }
    return rs;
  }

  public List<Pair<Integer, Pair<Double, Boolean>>> compute(final Table table,
      final int... skipColumns) throws IOException, ClassNotFoundException {
    return compute(table, null, skipColumns);
  }

  private void computeWSScores(final Table table, final List<TColumnFeature> featuresOfNEColumns)
      throws IOException, ClassNotFoundException {
    LOG.debug("Computing web search matching (total rows " + table.getNumRows());

    DoubleMatrix2D scores;
    if (this.tRowRanker != null) {
      scores = this.featureGenerator.setWSScores(featuresOfNEColumns, table, this.tRowRanker,
          StoppingCriteriaInstantiator.instantiate(this.stoppingCriteriaClassname,
              this.stoppingCriteriaParams),
          1);
    } else {
      scores = this.featureGenerator.setWSScores(featuresOfNEColumns, table);
    }
    double total = 0.0;
    for (final TColumnFeature cf : featuresOfNEColumns) {
      for (int row = 0; row < scores.rows(); row++) {
        total += scores.get(row, cf.getColId());
      }
      cf.setWebSearchScore(total);
      total = 0.0;
    }
  }

  /*
   * private void tiebreaker_reset( List<TColumnFeature> allNEColumnCandidates, Map<Integer,
   * ObjObj<Double, Boolean>> inferenceScores) { List<Integer> ties = new ArrayList<Integer>();
   * double maxScore = 0; for (Map.Entry<Integer, ObjObj<Double, Boolean>> e :
   * inferenceScores.entrySet()) { if (e.getValue().getMainObject() > maxScore) { maxScore =
   * e.getValue().getMainObject(); } } for (Map.Entry<Integer, ObjObj<Double, Boolean>> e :
   * inferenceScores.entrySet()) { if (e.getValue().getMainObject() == maxScore) {
   * ties.add(e.getKey()); } }
   *
   * if (ties.size() > 1) {
   *
   * double max = 0; int best = 0; for (int i : ties) { for (TColumnFeature cf :
   * allNEColumnCandidates) { if (cf.getColId() == i) { double sum = cf.getUniqueCellCount() +
   * cf.getCMScore() + cf.getUniqueTokenCount() + cf.getWSScore() - (cf.getEmptyCellCount() /
   * (double) cf.getNumRows());
   *
   * if (sum > max) { max = sum; best = i; } break; } } } // try {
   * inferenceScores.get(best).setMainObject( inferenceScores.get(best).getMainObject() + 1.0 ); //
   * } catch (NullPointerException n) { // System.out.println(); // } } }
   */
  // key: col id; value: computeElementScores
  // currently performs following scoring: diversity; context computeElementScores;
  // 1st ne column; acronym column checker; search
  // results are collected as number of votes by each dimension
  @SuppressWarnings("unused") // May be useful later.
  private Map<Integer, Pair<Double, Boolean>> infer_multiFeatures_vote(
      final List<TColumnFeature> allNEColumnCandidates) {
    final Map<Integer, Pair<Double, Boolean>> votes = new HashMap<>();
    // a. vote by diversity computeElementScores
    Collections.sort(allNEColumnCandidates, (o1, o2) -> {
      final int compared = new Double(o2.getUniqueCellCount()).compareTo(o1.getUniqueCellCount());
      if (compared == 0) {
        return new Double(o2.getUniqueTokenCount()).compareTo(o1.getUniqueTokenCount());
      }
      return compared;
    });
    double maxDiversityScore = -1.0;
    for (final TColumnFeature cf : allNEColumnCandidates) {
      final double diversity = cf.getUniqueTokenCount() + cf.getUniqueCellCount();
      if ((diversity >= maxDiversityScore) && (diversity != 0)) {
        maxDiversityScore = diversity;
        votes.put(cf.getColId(), new Pair<>(1.0, false));
      } else {
        break; // already sorted, so following this there shouldnt be higher diversity scores
      }
    }


    // b. vote by 1st ne column
    for (final TColumnFeature cf : allNEColumnCandidates) {
      if (cf.isFirstNEColumn()) {
        Pair<Double, Boolean> entry = votes.get(cf.getColId());
        entry = entry == null ? new Pair<>(0.0, false) : entry;
        Double vts = entry.getKey();
        vts = vts + 1.0;
        entry = new Pair<>(vts, entry.getValue());
        votes.put(cf.getColId(), entry);
        break;
      }
    }
    // c. vote by context matcher
    Collections.sort(allNEColumnCandidates,
        (o1, o2) -> new Double(o2.getCMScore()).compareTo(o1.getCMScore()));
    double maxContextMatchScore = -1.0;
    for (final TColumnFeature cf : allNEColumnCandidates) {
      if ((cf.getCMScore() >= maxContextMatchScore) && (cf.getCMScore() != 0)) {
        maxContextMatchScore = cf.getCMScore();
        Pair<Double, Boolean> entry = votes.get(cf.getColId());
        entry = entry == null ? new Pair<>(0.0, false) : entry;
        Double vts = entry.getKey();
        vts = vts + 1.0;
        entry = new Pair<>(vts, entry.getValue());
        votes.put(cf.getColId(), entry);
      } else {
        break;
      }
    }
    // d. vote by acronym columns
    for (final TColumnFeature cf : allNEColumnCandidates) {
      if (cf.isAcronymColumn()) {
        Pair<Double, Boolean> entry = votes.get(cf.getColId());
        entry = entry == null ? new Pair<>(0.0, false) : entry;
        Double vts = entry.getKey();
        vts = vts - 1.0;
        entry = new Pair<>(vts, true);
        votes.put(cf.getColId(), entry);
      }
    }

    // e. vote by search matcher
    Collections.sort(allNEColumnCandidates,
        (o1, o2) -> new Double(o2.getWSScore()).compareTo(o1.getWSScore()));
    double maxSearchMatchScore = -1.0;
    for (final TColumnFeature cf : allNEColumnCandidates) {
      if ((cf.getWSScore() >= maxSearchMatchScore) && (cf.getWSScore() != 0)) {
        maxSearchMatchScore = cf.getWSScore();
        Pair<Double, Boolean> entry = votes.get(cf.getColId());
        entry = entry == null ? new Pair<>(0.0, false) : entry;
        Double vts = entry.getKey();
        vts = vts + 1.0;
        entry = new Pair<>(vts, entry.getValue());
        votes.put(cf.getColId(), entry);
      } else {
        break;
      }
    }

    for (final TColumnFeature cf : allNEColumnCandidates) {
      if (votes.containsKey(cf.getColId())) {
        continue;
      }
      votes.put(cf.getColId(), new Pair<>(0.0, false));
    }
    return votes;
  }


  private void normalizeScores(final List<TColumnFeature> allNEColumnCandidates) {
    // c. context matcher
    Collections.sort(allNEColumnCandidates,
        (o1, o2) -> new Double(o2.getCMScore()).compareTo(o1.getCMScore()));
    final double maxCMScore = allNEColumnCandidates.get(0).getCMScore();
    if (maxCMScore > 0) {
      for (final TColumnFeature cf : allNEColumnCandidates) {
        final double rel_score = cf.getCMScore() / maxCMScore;
        cf.setContextMatchScore(rel_score);
      }
    }

    // e. vote by search matcher
    Collections.sort(allNEColumnCandidates,
        (o1, o2) -> new Double(o2.getWSScore()).compareTo(o1.getWSScore()));
    final double maxWSScore = allNEColumnCandidates.get(0).getWSScore();
    if (maxWSScore > 0) {
      for (final TColumnFeature cf : allNEColumnCandidates) {
        final double rel_score = cf.getWSScore() / maxWSScore;
        cf.setWebSearchScore(rel_score);
      }
    }
  }

  // only keep TColumnFeatures that correspond to an NE column
  private List<TColumnFeature> selectOnlyNEColumnFeatures(
      final List<TColumnFeature> allColumnFeatures) {
    final List<TColumnFeature> neColumns = new ArrayList<>();
    for (final TColumnFeature cf : allColumnFeatures) {
      if (cf.getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY)) {
        neColumns.add(cf);
      }
    }
    // EXCEPTION: what if no NE columns found? Add any columns that are short_text
    if (neColumns.size() == 0) {
      for (final TColumnFeature cf : allColumnFeatures) {
        if (cf.getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.SHORT_TEXT)) {
          neColumns.add(cf);
        }
      }
    }
    return neColumns;
  }

}
