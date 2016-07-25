package cz.cuni.mff.xrg.odalic.feedbacks.input;

import uk.ac.shef.dcs.sti.core.model.Table;

/**
 * Interface for {@link Table} to {@link Input} conversion.
 * 
 * @author Václav Brodec
 *
 */
public interface TableToInputAdapter {
  Input toInput(Table table);
}
