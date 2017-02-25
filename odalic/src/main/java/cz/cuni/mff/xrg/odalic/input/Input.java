package cz.cuni.mff.xrg.odalic.input;

import java.util.List;

import cz.cuni.mff.xrg.odalic.positions.CellPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.RowPosition;

/**
 * Input interface.
 *
 * @author Václav Brodec
 *
 */
public interface Input {
  String at(CellPosition position);

  int columnsCount();

  String headerAt(ColumnPosition position);

  List<String> headers();

  String identifier();

  List<String> rowAt(RowPosition position);

  List<List<String>> rows();

  int rowsCount();
}
