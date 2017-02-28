package uk.ac.shef.dcs.sti.core.algorithm;

import java.util.HashSet;
import java.util.Set;

import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

/**
 */
public abstract class SemanticTableInterpreter {

  private Set<Integer> ignoreCols;
  private Set<Integer> mustdoColumns;

  public SemanticTableInterpreter() {

  }

  public SemanticTableInterpreter(final int[] ignoreColumns, final int[] mustdoColumns) {
    this.ignoreCols = new HashSet<>();
    for (final int i : ignoreColumns) {
      this.ignoreCols.add(i);
    }
    this.mustdoColumns = new HashSet<>();
    for (final int i : mustdoColumns) {
      this.mustdoColumns.add(i);
    }
  }

  protected Set<Integer> getIgnoreColumns() {
    return this.ignoreCols;
  }

  protected Set<Integer> getMustdoColumns() {
    return this.mustdoColumns;
  }



  protected boolean isCompulsoryColumn(final Integer i) {
    if (i != null) {
      return this.mustdoColumns.contains(i);
    }
    return false;
  }

  public void setIgnoreColumns(final Set<Integer> ignoreCols) {
    this.ignoreCols = ignoreCols;
  }

  public abstract TAnnotation start(Table table, boolean relationLearning) throws STIException;

  public abstract TAnnotation start(Table table, boolean statistical, Constraints constraints)
      throws STIException;
}
