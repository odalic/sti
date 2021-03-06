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

/**

 */
public class OSPD_nonEmpty extends TContentCellRanker {
  @Override
  public List<List<Integer>> select(final Table table, final int fromCol, final Set<Integer> subCols) {
    final List<List<Integer>> rs = new ArrayList<List<Integer>>();

    if (STIConstantProperty.ENFORCE_OSPD && !subCols.contains(fromCol)) {
      // firstly group by one-sense-per-discourse
      final Map<String, List<Integer>> grouped = new HashMap<>();
      for (int r = 0; r < table.getNumRows(); r++) {
        final TCell tcc = table.getContentCell(r, fromCol);
        final String text = tcc.getText();
        if (text.length() > 0) {
          List<Integer> group = grouped.get(text);
          if (group == null) {
            group = new ArrayList<>();
          }
          group.add(r);
          grouped.put(text, group);
        }
      }

      final Map<List<Integer>, Integer> countNonEmpty = new HashMap<List<Integer>, Integer>();
      // then make selection
      for (final Map.Entry<String, List<Integer>> entry : grouped.entrySet()) {
        final List<Integer> rows = entry.getValue();

        int count_non_emtpy = 0;
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
          }
        }
        countNonEmpty.put(rows, count_non_emtpy);
        if (rows.size() > 0) {
          rs.add(rows);
        }


      }

      Collections.sort(rs,
          (o1, o2) -> new Integer(countNonEmpty.get(o2)).compareTo(countNonEmpty.get(o1)));

    } else {
      final Map<Integer, Integer> scores = new LinkedHashMap<Integer, Integer>();
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
        scores.put(r, count_non_empty);
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
