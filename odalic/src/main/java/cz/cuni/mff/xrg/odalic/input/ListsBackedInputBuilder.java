package cz.cuni.mff.xrg.odalic.input;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Incrementally, row by row, helps to produce the complete {@link ListsBackedInput}.
 *
 * @author Jan Váňa
 * @author Václav Brodec
 *
 */
public final class ListsBackedInputBuilder implements InputBuilder {

  private String fileIdentifier;
  private final List<String> headers = new ArrayList<>();
  private final List<List<String>> rows = new ArrayList<>();

  public ListsBackedInputBuilder() {}

  public ListsBackedInputBuilder(final Input initialInput) {
    setFileIdentifier(initialInput.identifier());
    this.headers.addAll(initialInput.headers());
    for (final List<String> rowList : initialInput.rows()) {
      final List<String> newList = new ArrayList<>();
      newList.addAll(rowList);
      this.rows.add(newList);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.input.InputBuilder#build()
   */
  @Override
  public Input build() {
    return new ListsBackedInput(this.fileIdentifier, this.headers, this.rows);
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.input.InputBuilder#clear()
   */
  @Override
  public void clear() {
    this.fileIdentifier = null;
    this.headers.clear();
    this.rows.clear();
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.input.InputBuilder#insertCell(java.lang.String, int, int)
   */
  @Override
  public void insertCell(final String value, final int rowIndex, final int columnIndex) {
    while (this.rows.size() <= rowIndex) {
      this.rows.add(new ArrayList<>());
    }

    insertToList(this.rows.get(rowIndex), value, columnIndex);
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.input.InputBuilder#insertHeader(java.lang.String, int)
   */
  @Override
  public void insertHeader(final String value, final int position) {
    insertToList(this.headers, value, position);
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.input.InputBuilder#insertToList(java.util.List, java.lang.String,
   * int)
   */
  @Override
  public void insertToList(final List<String> list, final String value, final int position) {
    while (list.size() <= position) {
      list.add(null);
    }

    list.set(position, value);
  }

  void setFileIdentifier(final String fileIdentifier) {
    Preconditions.checkNotNull(fileIdentifier);

    this.fileIdentifier = fileIdentifier;
  }
}
