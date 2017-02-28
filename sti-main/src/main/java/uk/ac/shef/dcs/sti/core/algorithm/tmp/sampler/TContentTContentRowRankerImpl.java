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
 */
public class TContentTContentRowRankerImpl extends TContentRowRanker {
  @Override
  public int[] select(final Table table) {
    final int[] rs = new int[table.getNumRows()];

    final Map<Integer, Integer> scores = new LinkedHashMap<Integer, Integer>();
    for (int i = 0; i < table.getNumRows(); i++) {
      int count_non_empty = 0;
      for (int col = 0; col < table.getNumCols(); col++) {
        final TCell tcc = table.getContentCell(i, col);
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
      scores.put(i, count_non_empty);
    }

    final List<Integer> list = new ArrayList<Integer>(scores.keySet());
    Collections.sort(list, (o1, o2) -> scores.get(o2).compareTo(scores.get(o1)));

    for (int i = 0; i < list.size(); i++) {
      rs[i] = i;
    }

    return rs;
  }
}
