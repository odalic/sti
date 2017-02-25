package cz.cuni.mff.xrg.odalic.input;

import com.google.common.base.Preconditions;

import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TColumnHeader;
import uk.ac.shef.dcs.sti.core.model.Table;

/**
 * Converts the {@link Table} to {@link ListsBackedInputBuilder}.
 *
 * @author Jan Váňa
 *
 */
public final class TableToListsBackedInputAdapter implements TableToInputAdapter {

  private final ListsBackedInputBuilder builder;

  public TableToListsBackedInputAdapter(final ListsBackedInputBuilder builder) {
    Preconditions.checkNotNull(builder);

    this.builder = builder;
  }

  @Override
  public Input toInput(final Table table) {
    this.builder.clear();
    this.builder.setFileIdentifier(table.getSourceId());

    for (int columnIndex = 0; columnIndex < table.getNumHeaders(); columnIndex++) {
      final TColumnHeader header = table.getColumnHeader(columnIndex);
      this.builder.insertHeader(header.getHeaderText(), columnIndex);
    }

    for (int columnIndex = 0; columnIndex < table.getNumCols(); columnIndex++) {
      for (int rowIndex = 0; rowIndex < table.getNumRows(); rowIndex++) {
        final TCell cell = table.getContentCell(rowIndex, columnIndex);
        this.builder.insertCell(cell.getText(), rowIndex, columnIndex);
      }
    }

    return this.builder.build();
  }

}
