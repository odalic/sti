package cz.cuni.mff.xrg.odalic.input;

import java.util.List;
import java.util.UUID;

import javax.annotation.concurrent.Immutable;

import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TColumnHeader;
import uk.ac.shef.dcs.sti.core.model.Table;

/**
 * The default {@link InputToTableAdapter} implementation.
 *
 * @author Jan Váňa
 *
 */
@Immutable
public class DefaultInputToTableAdapter implements InputToTableAdapter {

  /*
   * (non-Javadoc)
   * 
   * @see
   * cz.cuni.mff.xrg.odalic.input.InputToTableAdapter#toTable(cz.cuni.mff.xrg.odalic.input.Input)
   */
  @Override
  public Table toTable(final Input input) {
    final Table result = new Table(UUID.randomUUID().toString(), input.identifier(),
        input.rowsCount(), input.columnsCount());

    int columnIndex = 0;
    for (final String value : input.headers()) {
      final TColumnHeader header = new TColumnHeader(value);
      result.setColumnHeader(columnIndex, header);

      columnIndex++;
    }

    int rowIndex = 0;
    for (final List<String> row : input.rows()) {
      columnIndex = 0;
      for (final String value : row) {
        final TCell cell = new TCell(value);
        result.setContentCell(rowIndex, columnIndex, cell);

        columnIndex++;
      }
      rowIndex++;
    }

    return result;
  }

}
