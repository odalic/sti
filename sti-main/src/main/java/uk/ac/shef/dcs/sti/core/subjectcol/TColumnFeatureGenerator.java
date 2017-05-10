package uk.ac.shef.dcs.sti.core.subjectcol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentRowRanker;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.stopping.StoppingCriteria;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TColumnHeader;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.util.Cache;
import uk.ac.shef.dcs.util.StringUtils;
import uk.ac.shef.dcs.websearch.WebSearchException;
import uk.ac.shef.dcs.websearch.WebSearchFactory;

/**

 */
public class TColumnFeatureGenerator {
  /**
   * work out the ColumnDataTypes for every column in the table. For each column a list of candidate
   * ColumnDataTypes is created and attached. The list is UN-sorted
   *
   * @param table
   */
  public static void setColumnDataTypes(final Table table) {
    for (int col = 0; col < table.getNumCols(); col++) {
      final Map<DataTypeClassifier.DataType, TColumnDataType> types = new HashMap<>();// list to
                                                                                      // hold
                                                                                      // candidate
                                                                                      // data types
      final List<String> numbers = new ArrayList<>(); // list to hold numbers that are potentially
      // indexes of rows. The list will be then analyze, to decide if it is a continuous
      // incremental sequence of numbers. if so this column is assume to be index column
      boolean hasParagraph = false;

      for (int row = 0; row < table.getNumRows(); row++) {
        final TCell tcc = table.getContentCell(row, col);
        final String textContent = tcc.getText();
        if (textContent != null) {
          final DataTypeClassifier.DataType dt = DataTypeClassifier.classify(textContent);
          tcc.setType(dt);
          if (dt.equals(DataTypeClassifier.DataType.NUMBER)) {
            numbers.add(StringUtils.toAlphaNumericWhitechar(textContent).trim());
          }
          if (dt.equals(DataTypeClassifier.DataType.LONG_TEXT)) {
            hasParagraph = true;
          }

          TColumnDataType cdt = types.get(dt);
          if (cdt == null) {
            cdt = new TColumnDataType(dt, 0);
          }
          cdt.setSupportingRows(cdt.getSupportingRows() + 1);
          types.put(dt, cdt);
        }
      }

      // if any row is PARAGRAPH, it overwrites all other rows
      // the column can only te long paragraph
      if (hasParagraph) {
        types.clear();
        final DataTypeClassifier.DataType longText = DataTypeClassifier.DataType.LONG_TEXT;
        types.put(longText, new TColumnDataType(longText, numbers.size()));
      } else {// check if the most frequent type is ordered numbers.
        final List<TColumnDataType> sortedTypes = new ArrayList<>(types.values());
        Collections.sort(sortedTypes);
        if ((numbers.size() != 0)
            && sortedTypes.get(0).getType().equals(DataTypeClassifier.DataType.NUMBER)) {
          final boolean ordered =
              DataTypeClassifier.isOrderedNumber(numbers.toArray(new String[0]));
          if (ordered) {
            types.clear();
            final DataTypeClassifier.DataType orderedNumber =
                DataTypeClassifier.DataType.ORDERED_NUMBER;
            types.put(orderedNumber, new TColumnDataType(orderedNumber, numbers.size()));
          }
        }
      }
      table.getColumnHeader(col).setType(new ArrayList<>(types.values()));
    }
  }

  private final CMScorer cmScorer;
  private final WSScorer wsScorer;


  private final NLPTools nlpTools;

  public TColumnFeatureGenerator(final Cache cache, final String nlpResource,
      final List<String> stopWords, final String webSearchPropFile)
      throws IOException, WebSearchException {
    this.cmScorer = new CMScorer(nlpResource);
    this.wsScorer = new WSScorer(cache,
        new WebSearchFactory().createInstance(webSearchPropFile), stopWords);
    this.nlpTools = NLPTools.getInstance(nlpResource);
  }


  /**
   * make a guess if this column contains only acronym values
   *
   * if, excluding empty cells, #of cells that are acronyms are more than those that are not, a
   * column is then considered to contain only acronym
   *
   * @param featuresOfNEColumns
   * @param table
   */
  protected void setAcronymColumnBoolean(final List<TColumnFeature> featuresOfNEColumns,
      final Table table) {
    for (final TColumnFeature cf : featuresOfNEColumns) {
      final int col = cf.getColId();
      int countAcronym_or_Code = 0;
      for (int r = 0; r < table.getNumRows(); r++) {
        final String cellContent =
            table.getContentCell(r, col).getText().replaceAll("\\s+", " ").trim();
        if (cellContent.length() == 0) {
          continue;
        }
        if (cellContent.length() < 15) {
          int countWhiteSpace = 0, countLetters = 0, countDigits = 0;
          boolean letters_are_all_cap = true;
          for (int index = 0; index < cellContent.length(); index++) {
            final char c = cellContent.charAt(index);
            if (Character.isWhitespace(c)) {
              countWhiteSpace++;
            } else {
              if (Character.isLetter(c)) {
                countLetters++;
                if (!Character.isUpperCase(c)) {
                  letters_are_all_cap = false;
                }
              } else if (Character.isDigit(c)) {
                countDigits++;
              }
            }
          }

          // int countSymbols = countNonWhiteSpace - countLetters - countDigits;
          if ((countWhiteSpace == 0) && (countDigits > 0) && (countLetters > 0)) { // no white
                                                                                   // space, mixture
                                                                                   // of letters
                                                                                   // (whatever
                                                                                   // case) and
                                                                                   // digits
            countAcronym_or_Code++;
            // System.out.println(cellContent+" \tno white space, mixture of letters and digits");
          } else if ((countWhiteSpace == 0) && (countLetters > 0) && letters_are_all_cap
              && (cellContent.length() < 6)) { // no white space, all uppercase letters (total < 6)
            countAcronym_or_Code++;
            // System.out.println(cellContent+" \tno white space, all uppercase letters (total <
            // 6)");
          } else if ((countWhiteSpace == 1) && letters_are_all_cap) { // 1 white space, letters must
                                                                      // all be uppercase
            countAcronym_or_Code++;
            // System.out.println(cellContent+" \t1 white space, letters must all be uppercase");
          }
        }


      }
      if (countAcronym_or_Code > (table.getNumRows() - cf.getEmptyCellCount()
          - countAcronym_or_Code)) {
        cf.setAcronymColumn(true);
      }
    }

  }

  // how does each header computeElementScores against the table contexts
  protected void setCMScores(final List<TColumnFeature> features, final Table table) {
    final int[] cols = new int[features.size()];

    for (int c = 0; c < features.size(); c++) {
      final int col = features.get(c).getColId();
      cols[c] = col;
    }

    final Map<Integer, Double> scores = this.cmScorer.score(table, cols);
    for (final TColumnFeature cf : features) {
      Double s = scores.get(cf.getColId());
      s = s == null ? 0 : s;
      cf.setContextMatchScore(s);
    }


  }

  // count number of empty cells in this column
  protected void setEmptyCellCount(final List<TColumnFeature> features, final Table table) {
    for (final TColumnFeature cf : features) {
      final int col = cf.getColId();

      int countEmpty = 0;
      for (int r = 0; r < table.getNumRows(); r++) {
        final String textContent = table.getContentCell(r, col).getText();
        if ((textContent == null) || (textContent.length() == 0)) {
          countEmpty++;
        }
      }
      cf.setEmptyCellCount(countEmpty);
    }
  }

  // check the syntactic feature (POS) of the header title. invalid POS include: prep (in theory
  // only noun is valid. but
  // that may over-eliminate true pos
  protected void setInvalidHeaderTextSyntax(final List<TColumnFeature> allNEColumnCandidates,
      final Table table) {
    for (final TColumnFeature cf : allNEColumnCandidates) {
      final int col = cf.getColId();
      final String headerText = table.getColumnHeader(col).getHeaderText();

      final String[] tags =
          this.nlpTools.getPosTagger().tag(headerText.toLowerCase().split("\\s+"));
      if (tags[tags.length - 1].equals("IN") || tags[tags.length - 1].equals("TO")) {
        cf.setInvalidPOS(true);
      }
    }

  }

  // which column is the first NE column
  protected void setIsFirstNEColumn(final List<TColumnFeature> features) {
    for (final TColumnFeature cf : features) {
      if (cf.getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY)) {
        cf.setFirstNEColumn(true);
        break;
      }
    }
  }

  /**
   * for each TColumn in the table, compute the most frequent data type for that column and set this
   * value in the TColumnFeature object corresponding to that column
   *
   * @param features
   * @param table
   */
  protected void setMostFrequentDataTypes(final List<TColumnFeature> features, final Table table) {
    for (final TColumnFeature cf : features) {
      final int col = cf.getColId();
      final TColumnHeader header = table.getColumnHeader(col);
      final List<TColumnDataType> types = header.getTypes();
      Collections.sort(types);
      cf.setMostFrequentDataType(types.get(0));
    }
  }

  // is there a column as the only NE column in the table
  protected int setOnlyNEColumn(final List<TColumnFeature> features) {
    final List<Integer> indexes = new ArrayList<>();
    for (int i = 0; i < features.size(); i++) {
      if (features.get(i).getMostFrequentDataType().getType()
          .equals(DataTypeClassifier.DataType.NAMED_ENTITY)) {
        indexes.add(i);
      }
    }
    if (indexes.size() == 1) {
      features.get(indexes.get(0)).setOnlyNEColumn(true);
      return features.get(indexes.get(0)).getColId();
    }
    return -1;
  }

  // if multiple ne columns, is there a column that is the only non-empty one?
  protected int setOnlyNonDuplicateNEColumn(final List<TColumnFeature> features,
      final Table table) {
    int onlyNonDuplicateNECol = -1, num = 0;
    for (int index = 0; index < features.size(); index++) {
      final TColumnFeature cf = features.get(index);
      if ((cf.getUniqueCellCount() == 1.0) && !cf.isAcronymColumn()
          && (cf.getMostFrequentDataType().getSupportingRows() == table.getNumRows())) {
        num++;
        if (onlyNonDuplicateNECol == -1) {
          onlyNonDuplicateNECol = index;
        } else {
          break;
        }
      }
    }

    if ((onlyNonDuplicateNECol != -1) && (num == 1)) {
      final TColumnFeature f = features.get(onlyNonDuplicateNECol);
      if (!f.isAcronymColumn()) {
        f.setIsOnlyNonDuplicateNEColumn(true);
        return f.getColId();
      }
    }
    return -1;
  }

  // if multiple ne columns, is there a column that is the only non-empty one?
  protected int setOnlyNonEmptyNEColumn(final List<TColumnFeature> features) {
    int onlyNonEmptyNECol = -1, num = 0;
    for (int index = 0; index < features.size(); index++) {
      final TColumnFeature cf = features.get(index);
      if (cf.getEmptyCellCount() == 0 /* && !cf.isAcronymColumn() */) {
        num++;
        if (onlyNonEmptyNECol == -1) {
          onlyNonEmptyNECol = index;
        } else {
          break;
        }
      }
    }
    if ((onlyNonEmptyNECol != -1) && (num == 1)) {
      final TColumnFeature f = features.get(onlyNonEmptyNECol);
      if (!f.isAcronymColumn()) {
        f.setIsOnlyNonEmptyNEColumn(true);
        return f.getColId();
      }
    }
    return -1;
  }


  // how many unique values do we have in a column
  protected void setUniqueValueCount(final List<TColumnFeature> features, final Table table) {
    for (final TColumnFeature cf : features) {
      final int col = cf.getColId();
      final Set<String> uniqueValues_onRows = new HashSet<>();
      final Set<String> uniqueTokens_onRows = new HashSet<>();
      int totalTokens = 0;
      for (int r = 0; r < table.getNumRows(); r++) {
        final TCell c = table.getContentCell(r, col);
        uniqueValues_onRows.add(c.getText());

        for (final String tok : c.getText().split("\\s+")) {
          uniqueTokens_onRows.add(tok.trim());
          totalTokens++;
        }
      }
      final double diversity_1 = (double) uniqueValues_onRows.size() / table.getNumRows();
      final double diversity_2 = (double) uniqueTokens_onRows.size() / table.getNumRows()
          / ((double) totalTokens / table.getNumRows());
      cf.setUniqueCellCount(diversity_1);
      cf.setUniqueTokenCount(diversity_2);
    }
  }

  // how does each cell on each row computeElementScores against a websearch result?
  protected DoubleMatrix2D setWSScores(final List<TColumnFeature> features, final Table table)
      throws IOException {
    final DoubleMatrix2D scores = new SparseDoubleMatrix2D(table.getNumRows(), table.getNumCols());
    final List<Integer> searchableCols = new ArrayList<Integer>();// which columns contain values
                                                                  // that are searchable? (numbers
                                                                  // ignored, for example)
    for (int i = 0; i < features.size(); i++) {
      final DataTypeClassifier.DataType type = features.get(i).getMostFrequentDataType().getType();
      if (type.equals(DataTypeClassifier.DataType.NAMED_ENTITY)
          || type.equals(DataTypeClassifier.DataType.SHORT_TEXT)) {
        searchableCols.add(features.get(i).getColId());
      }
    }

    // then search row-by-row
    for (int r = 0; r < table.getNumRows(); r++) {
      // prepare search string
      final String[] values_on_the_row = new String[searchableCols.size()];
      for (int c = 0; c < searchableCols.size(); c++) {
        final int colId = searchableCols.get(c);
        final TCell cell = table.getContentCell(r, colId);
        values_on_the_row[c] = this.wsScorer.normalize(cell.getText());
      }

      // perform search and compute matching scores
      final Map<String, Double> scores_on_the_row = this.wsScorer.score(values_on_the_row);

      // compute search results against each column in searchableCols
      for (int index = 0; index < searchableCols.size(); index++) {
        final int colId = searchableCols.get(index);
        final String normalizedCellContent = values_on_the_row[index];
        if (normalizedCellContent.length() < 1) {
          continue;
        }

        Double search_score = scores_on_the_row.get(normalizedCellContent);
        search_score = search_score == null ? 0 : search_score;
        scores.set(r, colId, search_score);
      }
    }

    return scores;
  }

  // since using web search is expensive, we can use data sampling technique to incrementally
  // computeElementScores main column
  protected DoubleMatrix2D setWSScores(final List<TColumnFeature> features, final Table table,
      final TContentRowRanker sampleSelector, final StoppingCriteria stopper, final int minimumRows)
      throws IOException {

    if (minimumRows > table.getNumRows()) {
      return setWSScores(features, table);
    }

    final DoubleMatrix2D scores = new SparseDoubleMatrix2D(table.getNumRows(), table.getNumCols());
    final Map<Object, Double> state = new HashMap<>();

    final List<Integer> searchableCols = new ArrayList<>();// which columns contain values that are
                                                           // searchable? (numbers ignored, for
                                                           // example)
    for (final TColumnFeature feature : features) {
      final DataTypeClassifier.DataType type = feature.getMostFrequentDataType().getType();
      if (type.equals(DataTypeClassifier.DataType.NAMED_ENTITY)
          || type.equals(DataTypeClassifier.DataType.SHORT_TEXT)) {
        searchableCols.add(feature.getColId());
      }
    }


    final int[] rowRanking = sampleSelector.select(table);
    // then search row-by-row
    for (final int r : rowRanking) {
      // prepare search string
      final String[] values_in_the_cell = new String[searchableCols.size()];
      for (int c = 0; c < searchableCols.size(); c++) {
        final int colId = searchableCols.get(c);
        final TCell cell = table.getContentCell(r, colId);
        values_in_the_cell[c] = this.wsScorer.normalize(cell.getText());
      }

      // perform search and compute matching scores
      final Map<String, Double> ws_on_row = this.wsScorer.score(values_in_the_cell);

      // compute search results against each column in searchableCols
      for (int index = 0; index < searchableCols.size(); index++) {
        final int colId = searchableCols.get(index);
        final String normalizedCellContent = values_in_the_cell[index];
        if (normalizedCellContent.length() < 1) {
          continue;
        }

        Double search_score = ws_on_row.get(normalizedCellContent);
        search_score = search_score == null ? 0 : search_score;
        scores.set(r, colId, search_score);

        // also update State
        Double score_in_the_state = state.get(colId);
        score_in_the_state = score_in_the_state == null ? 0 : score_in_the_state;
        score_in_the_state = score_in_the_state + search_score;
        state.put(colId, score_in_the_state);
      }

      if (/* rows>=minimumRows && */stopper.stop(state, table.getNumRows())) {
        break;
      }
    }

    return scores;
  }


}
