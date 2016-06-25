package cz.cuni.mff.xrg.odalic.feedbacks.input;

import java.util.List;

import cz.cuni.mff.xrg.odalic.positions.CellPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.RowPosition;

public interface Input {
  String at(CellPosition position);
  String headerAt(ColumnPosition position);
  List<String> rowAt(RowPosition position);
  List<String> columnAt(ColumnPosition position);
  int rowsCount();
  int columnsCount();
  List<String> headers();
  List<List<String>> rows();
  List<List<String>> columns();
}
