package uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;

/**
 * Created with IntelliJ IDEA. User: zqz Date: 27/05/14 Time: 11:09 To change this template use File
 * | Settings | File Templates.
 */
public class OSPD_nameLength extends TContentCellRanker {
  @Override
  public List<List<Integer>> select(final Table table, final int fromCol, final int subCol) {
    final List<List<Integer>> rs = new ArrayList<List<Integer>>();


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

    return rs;
  }
}
