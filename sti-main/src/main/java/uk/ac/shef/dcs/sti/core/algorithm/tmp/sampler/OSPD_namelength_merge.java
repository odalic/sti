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

/**
 * Created with IntelliJ IDEA. User: zqz Date: 07/07/14 Time: 14:44 To change this template use File
 * | Settings | File Templates.
 */
public class OSPD_namelength_merge extends TContentCellRanker {


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

      final Map<List<Integer>, Integer> countNameLength = new HashMap<List<Integer>, Integer>();


      // then make selection
      for (final Map.Entry<String, List<Integer>> entry : grouped.entrySet()) {
        final List<Integer> rows = entry.getValue();

        final TCell tcc = table.getContentCell(rows.get(0), fromCol);
        if (tcc.getType().equals(DataTypeClassifier.DataType.EMPTY)) {
          countNameLength.put(rows, 0);
          continue;
        }

        String text = tcc.getText();
        text = text.replaceAll("[\\-_/,]", " ").replace("\\s+", " ").trim();
        final int count_name_length = text.split("\\s+").length;
        countNameLength.put(rows, count_name_length);

        if (rows.size() > 0) {
          rs.add(rows);
        }

      }

      Collections.sort(rs,
          (o1, o2) -> new Integer(countNameLength.get(o2)).compareTo(countNameLength.get(o1)));

    } else {
      final Map<Integer, Integer> scores = new LinkedHashMap<Integer, Integer>();
      for (int r = 0; r < table.getNumRows(); r++) {
        int count_name_length = 0;
        final TCell tcc_at_focus = table.getContentCell(r, fromCol);
        if (tcc_at_focus.getType().equals(DataTypeClassifier.DataType.EMPTY)) {
          scores.put(r, 0);
          continue;
        }

        String text = tcc_at_focus.getText();
        text = text.replaceAll("[\\-_/,]", " ").replace("\\s+", " ").trim();
        count_name_length = text.split("\\s+").length;
        scores.put(r, count_name_length);
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
