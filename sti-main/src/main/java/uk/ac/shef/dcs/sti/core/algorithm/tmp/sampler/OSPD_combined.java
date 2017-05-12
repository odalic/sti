package uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.util.StringUtils;

/**
 * Created with IntelliJ IDEA. User: zqz Date: 27/05/14 Time: 11:20 To change this template use File
 * | Settings | File Templates.
 */
public class OSPD_combined extends TContentCellRanker {
  private List<String> stopwords = new ArrayList<String>();

  public OSPD_combined(final List<String> stopwords) {
    this.stopwords = stopwords;
  }

  @Override
  public List<List<Integer>> select(final Table table, final int fromCol, final Set<Integer> subCols) {
    final List<List<Integer>> rs = new ArrayList<List<Integer>>();

    if (STIConstantProperty.ENFORCE_OSPD && !subCols.contains(fromCol)) {
      // firstly group by one-sense-per-discourse
      final Map<String, List<Integer>> grouped = new HashMap<String, List<Integer>>();
      for (int r = 0; r < table.getNumRows(); r++) {
        final TCell tcc = table.getContentCell(r, fromCol);
        final String text = tcc.getText();
        if (text.length() > 0) {
          List<Integer> group = grouped.get(text);
          if (group == null) {
            group = new ArrayList<Integer>();
          }
          group.add(r);
          grouped.put(text, group);
        }
      }


      final Map<List<Integer>, Integer> countNonEmpty = new HashMap<List<Integer>, Integer>();
      int max_non_emtpy = 0;
      final Map<List<Integer>, Integer> countNonStopwords = new HashMap<List<Integer>, Integer>();
      int max_non_stopwords = 0;
      final Map<Integer, Integer> scores_name_length = new LinkedHashMap<Integer, Integer>();
      int max_name_length = 0;
      // then make selection
      for (final Map.Entry<String, List<Integer>> entry : grouped.entrySet()) {
        final List<Integer> rows = entry.getValue();

        int count_non_emtpy = 0;
        int count_non_stopwords = 0;
        int count_name_length = 0;
        for (int i = 0; i < rows.size(); i++) {
          for (int c = 0; c < table.getNumCols(); c++) {
            final TCell tcc = table.getContentCell(rows.get(i), c);
            if ((tcc.getType() != null)
                && !tcc.getType().equals(DataTypeClassifier.DataType.UNKNOWN)
                && !tcc.getType().equals(DataTypeClassifier.DataType.EMPTY)) {
              count_non_emtpy++;
            } else if (tcc.getType() == null) {
              final String text = tcc.getText().trim();
              if (text.length() > 0) {
                count_non_emtpy++;
              }
            }

            final List<String> tokens =
                StringUtils.splitToAlphaNumericTokens(tcc.getText().trim(), true);
            tokens.removeAll(this.stopwords);

            if (tokens.size() > 0) {
              count_non_stopwords += tokens.size();
            }
          }

        }
        countNonEmpty.put(rows, count_non_emtpy);
        if (count_non_emtpy > max_non_emtpy) {
          max_non_emtpy = count_non_emtpy;
        }
        countNonStopwords.put(rows, count_non_stopwords);
        if (count_non_stopwords > max_non_stopwords) {
          max_non_stopwords = count_non_stopwords;
        }
        final TCell tcc_at_focus = table.getContentCell(rows.get(0), fromCol);
        if (tcc_at_focus.getType().equals(DataTypeClassifier.DataType.EMPTY)) {
          for (final int r : rows) {
            scores_name_length.put(r, 0);
          }
          continue;
        }
        String text = tcc_at_focus.getText();
        text = text.replaceAll("[\\-_/,]", " ").replace("\\s+", " ").trim();
        count_name_length = text.split("\\s+").length;
        if (count_name_length > max_name_length) {
          max_name_length = count_name_length;
        }
        for (final int r : rows) {
          scores_name_length.put(r, count_name_length);
        }

        if (rows.size() > 0) {
          rs.add(rows);
        }
      }


      final Map<List<Integer>, Double> countNonEmpty_normalized =
          new HashMap<List<Integer>, Double>();
      final Map<List<Integer>, Double> countNonStopwords_normalized =
          new HashMap<List<Integer>, Double>();
      final Map<Integer, Double> scores_name_length_normalized =
          new LinkedHashMap<Integer, Double>();

      for (final Map.Entry<List<Integer>, Integer> e : countNonEmpty.entrySet()) {
        final double score = max_non_emtpy == 0 ? 0 : (double) e.getValue() / max_non_emtpy;
        countNonEmpty_normalized.put(e.getKey(), score);
      }
      for (final Map.Entry<List<Integer>, Integer> e : countNonStopwords.entrySet()) {
        final double score = max_non_stopwords == 0 ? 0 : (double) e.getValue() / max_non_stopwords;
        countNonStopwords_normalized.put(e.getKey(), score);
      }
      for (final Map.Entry<Integer, Integer> e : scores_name_length.entrySet()) {
        final double score = max_name_length == 0 ? 0 : (double) e.getValue() / max_name_length;
        scores_name_length_normalized.put(e.getKey(), score);
      }

      Collections.sort(rs, (o1, o2) -> {
        countNonEmpty_normalized.get(o1);
        countNonEmpty_normalized.get(o2);
        final double countnonstopwords1 = countNonStopwords_normalized.get(o1);
        final double countnonstopwords2 = countNonStopwords_normalized.get(o2);
        final double countnamelength1 = scores_name_length_normalized.get(o1.get(0));
        final double countnamelength2 = scores_name_length_normalized.get(o2.get(0));

        final double score1 = /* countnonempty1 + */ countnonstopwords1 + countnamelength1;
        final double score2 = /* countnonempty2 + */ countnonstopwords2 + countnamelength2;

        return new Double(score2).compareTo(score1);
      });


    } else {
      final Map<Integer, Integer> scores_non_empty_cells = new LinkedHashMap<Integer, Integer>();
      int max_non_emtpy = 0;
      for (int r = 0; r < table.getNumRows(); r++) {
        int count_non_empty = 0;
        final TCell tcc_at_focus = table.getContentCell(r, fromCol);
        if (tcc_at_focus.getType().equals(DataTypeClassifier.DataType.EMPTY)) {
          continue;
        }

        for (int c = 0; c < table.getNumCols(); c++) {
          final TCell tcc = table.getContentCell(r, c);
          if ((tcc.getType() != null) && !tcc.getType().equals(DataTypeClassifier.DataType.UNKNOWN)
              && !tcc.getType().equals(DataTypeClassifier.DataType.EMPTY)) {
            count_non_empty++;
          } else if (tcc.getType() == null) {
            final String cellText = tcc.getText().trim();
            if (cellText.length() > 0) {
              count_non_empty++;
            }
          }
        }
        if (count_non_empty > max_non_emtpy) {
          max_non_emtpy = count_non_empty;
        }

        scores_non_empty_cells.put(r, count_non_empty);
      }


      final Map<Integer, Integer> scores_non_stopwords = new LinkedHashMap<Integer, Integer>();
      int max_non_stopwords = 0;
      for (int r = 0; r < table.getNumRows(); r++) {
        int count_non_stopwords = 0;
        final TCell tcc_at_focus = table.getContentCell(r, fromCol);
        if (tcc_at_focus.getType().equals(DataTypeClassifier.DataType.EMPTY)) {
          continue;
        }

        for (int c = 0; c < table.getNumCols(); c++) {
          final TCell tcc = table.getContentCell(r, c);

          final List<String> tokens =
              StringUtils.splitToAlphaNumericTokens(tcc.getText().trim(), true);
          tokens.removeAll(this.stopwords);

          if (tokens.size() > 0) {
            count_non_stopwords += tokens.size();
          }
        }
        if (count_non_stopwords > max_non_stopwords) {
          max_non_stopwords = count_non_stopwords;
        }
        scores_non_stopwords.put(r, count_non_stopwords);
      }


      final Map<Integer, Integer> scores_name_length = new LinkedHashMap<Integer, Integer>();
      int max_name_length = 0;
      for (int r = 0; r < table.getNumRows(); r++) {
        int count_name_length = 0;
        final TCell tcc_at_focus = table.getContentCell(r, fromCol);
        if (tcc_at_focus.getType().equals(DataTypeClassifier.DataType.EMPTY)) {
          scores_name_length.put(r, 0);
          continue;
        }

        String text = tcc_at_focus.getText();
        text = text.replaceAll("[\\-_/,]", " ").replace("\\s+", " ").trim();
        count_name_length = text.split("\\s+").length;
        scores_name_length.put(r, count_name_length);
        if (count_name_length > max_name_length) {
          max_name_length = count_name_length;
        }
      }


      final Map<Integer, Double> countNonEmpty_normalized = new HashMap<Integer, Double>();
      final Map<Integer, Double> countNonStopwords_normalized = new HashMap<Integer, Double>();
      final Map<Integer, Double> scores_name_length_normalized =
          new LinkedHashMap<Integer, Double>();

      for (final Map.Entry<Integer, Integer> e : scores_non_empty_cells.entrySet()) {
        final double score = max_non_emtpy == 0 ? 0 : (double) e.getValue() / max_non_emtpy;
        countNonEmpty_normalized.put(e.getKey(), score);
      }
      for (final Map.Entry<Integer, Integer> e : scores_non_stopwords.entrySet()) {
        final double score = max_non_stopwords == 0 ? 0 : (double) e.getValue() / max_non_stopwords;
        countNonStopwords_normalized.put(e.getKey(), score);
      }
      for (final Map.Entry<Integer, Integer> e : scores_name_length.entrySet()) {
        final double score = max_name_length == 0 ? 0 : (double) e.getValue() / max_name_length;
        scores_name_length_normalized.put(e.getKey(), score);
      }

      final List<Integer> list = new ArrayList<Integer>(scores_non_empty_cells.keySet());
      Collections.sort(list, (o1, o2) -> {
        final double countnonempty1 = countNonEmpty_normalized.get(o1);
        final double countnonempty2 = countNonEmpty_normalized.get(o2);
        final double countnonstopwords1 = countNonStopwords_normalized.get(o1);
        final double countnonstopwords2 = countNonStopwords_normalized.get(o2);
        final double countnamelength1 = scores_name_length_normalized.get(o1);
        final double countnamelength2 = scores_name_length_normalized.get(o2);

        final double score1 = countnonempty1 + countnonstopwords1 + countnamelength1;
        final double score2 = countnonempty2 + countnonstopwords2 + countnamelength2;

        return new Double(score2).compareTo(score1);
      });


      for (int i = 0; i < list.size(); i++) {
        final List<Integer> block = new ArrayList<Integer>();
        block.add(i);
        rs.add(block);
      }
    }
    return rs;
  }
}
