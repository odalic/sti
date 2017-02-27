package uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.util.StringUtils;

/**
 * Created with IntelliJ IDEA. User: zqz Date: 27/05/14 Time: 10:34 To change this template use File
 * | Settings | File Templates.
 */
public class OSPD_contextWords extends TContentCellRanker {

  private List<String> stopwords = new ArrayList<String>();

  public OSPD_contextWords(final List<String> stopwords) {
    this.stopwords = stopwords;
  }

  @Override
  public List<List<Integer>> select(final Table table, final int fromCol, final int subCol) {
    final List<List<Integer>> rs = new ArrayList<List<Integer>>();

    if (STIConstantProperty.ENFORCE_OSPD && (fromCol != subCol)) {
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

      final Map<List<Integer>, Integer> countNonStopwords = new HashMap<List<Integer>, Integer>();


      // then make selection
      for (final Map.Entry<String, List<Integer>> entry : grouped.entrySet()) {
        final List<Integer> rows = entry.getValue();

        int count_non_stopwords = 0;
        for (int i = 0; i < rows.size(); i++) {
          for (int c = 0; c < table.getNumCols(); c++) {
            final TCell tcc = table.getContentCell(rows.get(i), c);

            final List<String> tokens =
                StringUtils.splitToAlphaNumericTokens(tcc.getText().trim(), true);
            tokens.removeAll(this.stopwords);

            if (tokens.size() > 0) {
              count_non_stopwords += tokens.size();
            }

          }
        }
        countNonStopwords.put(rows, count_non_stopwords);
        if (rows.size() > 0) {
          rs.add(rows);
        }


      }

      Collections.sort(rs,
          (o1, o2) -> new Integer(countNonStopwords.get(o2)).compareTo(countNonStopwords.get(o1)));

    } else {
      final Map<Integer, Integer> scores = new LinkedHashMap<Integer, Integer>();
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
        scores.put(r, count_non_stopwords);
      }

      final List<Integer> list = new ArrayList<Integer>(scores.keySet());
      Collections.sort(list, (o1, o2) -> scores.get(o2).compareTo(scores.get(o1)));

      for (int i = 0; i < list.size(); i++) {
        final List<Integer> block = new ArrayList<Integer>();
        block.add(i);
        rs.add(block);
      }
    }
    return rs;
  }
}
