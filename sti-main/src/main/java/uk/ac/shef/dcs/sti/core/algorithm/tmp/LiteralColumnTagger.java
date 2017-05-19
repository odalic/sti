package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import uk.ac.shef.dcs.kbproxy.ProxyException;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

/**
 */
public interface LiteralColumnTagger {
  void annotate(Table table, TAnnotation annotations, Integer... enColumnIndexes)
      throws ProxyException;

  void annotate(Table table, TAnnotation annotations, Constraints constraints, Integer... enColumnIndexes)
      throws ProxyException;

  void setIgnoreColumns(int... ignoreCols);
}
