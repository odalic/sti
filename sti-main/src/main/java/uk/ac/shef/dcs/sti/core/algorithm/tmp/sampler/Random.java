package uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;

/**
 * Created with IntelliJ IDEA. User: zqz Date: 27/05/14 Time: 12:27 To change this template use File
 * | Settings | File Templates.
 */
public class Random extends TContentCellRanker {


  public Random() {}

  @Override
  public List<List<Integer>> select(final Table table, final int fromCol, final Set<Integer> subCols) {
    final List<List<Integer>> rs = new ArrayList<List<Integer>>();


    final Map<Integer, Integer> scores = new LinkedHashMap<Integer, Integer>();
    for (int r = 0; r < table.getNumRows(); r++) {
      final TCell tcc_at_focus = table.getContentCell(r, fromCol);
      if (tcc_at_focus.getType().equals(DataTypeClassifier.DataType.EMPTY)) {
        continue;
      }

      scores.put(r, 1);
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
