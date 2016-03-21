package uk.ac.shef.dcs.oak.sti.algorithm.tm.selector;

import uk.ac.shef.dcs.oak.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.sti.rep.LTable;
import uk.ac.shef.dcs.oak.sti.rep.LTableContentCell;

import java.util.*;

/**
 * Selects certain sample set of rows (as it is it seems that it selects all rows)
 */
public class LTableContentRow_Sampler_nonEmpty extends RowSelector {
    /**
     * @param table
     * @return Array of row numbers
     */
    @Override
    public int[] select(LTable table) {
        int[] rs = new int[table.getNumRows()];

        final Map<Integer, Integer> scores = new LinkedHashMap<Integer, Integer>();
        for (int i = 0; i < table.getNumRows(); i++) {
            int count_non_empty = 0;
            for (int col = 0; col < table.getNumCols(); col++) {
                LTableContentCell tcc = table.getContentCell(i, col);
                if (tcc.getType() != null && !tcc.getType().equals(DataTypeClassifier.DataType.UNKNOWN) &&
                        !tcc.getType().equals(DataTypeClassifier.DataType.EMPTY))
                    count_non_empty++;
                else if (tcc.getType() == null) {
                    String cellText = tcc.getText().trim();
                    if (cellText.length() > 0)
                        count_non_empty++;
                }
            }
            scores.put(i, count_non_empty);
        }

        List<Integer> list = new ArrayList<Integer>(scores.keySet());
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return scores.get(o2).compareTo(scores.get(o1));
            }
        });

        for (int i = 0; i < list.size(); i++) {
            rs[i] = i;
        }

        return rs;
    }
}
