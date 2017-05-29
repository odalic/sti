package cz.cuni.mff.xrg.odalic.input;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import cz.cuni.mff.xrg.odalic.positions.CellPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.RowPosition;

/**
 * An {@link Input} implementation using a list of lists to store the cells.
 *
 * @author Václav Brodec
 * @author Jan Váňa
 */
@XmlRootElement(name = "input")
@XmlAccessorType(XmlAccessType.NONE)
@Immutable
public final class ListsBackedInput implements Input, Serializable {

  private static final long serialVersionUID = 4101912998363935336L;

  @XmlElement
  private final List<List<String>> rows;

  @XmlElement
  private final List<String> headers;

  @XmlElement
  private final String fileIdentifier;

  /**
   * Creates the list-backed input instance.
   *
   * @param fileIdentifier file ID
   * @param headers list of header
   * @param rows rows, the inner lists representing the rows can contain {@code null} values
   */
  public ListsBackedInput(final String fileIdentifier, final List<? extends String> headers,
      final List<? extends List<? extends String>> rows) {
    Preconditions.checkNotNull(fileIdentifier, "The fileIdentifier cannot be null!");
    Preconditions.checkNotNull(headers, "The headers cannot be null!");
    Preconditions.checkNotNull(rows, "The rows cannot be null!");

    this.fileIdentifier = fileIdentifier;

    this.headers = ImmutableList.copyOf(headers);

    final List<List<String>> mutableRows = new ArrayList<>(rows.size());
    for (final List<? extends String> row : rows) {
      mutableRows.add(Collections.unmodifiableList(new ArrayList<>(row)));
    }
    this.rows = Collections.unmodifiableList(mutableRows);
  }

  @Override
  public String at(final CellPosition position) {
    return this.rows.get(position.getRowIndex()).get(position.getColumnIndex());
  }

  @Override
  public int columnsCount() {
    return this.headers.size();
  }

  @Override
  public String headerAt(final ColumnPosition position) {
    return this.headers.get(position.getIndex());
  }

  @Override
  public List<String> headers() {
    return this.headers;
  }

  @Override
  public String identifier() {
    return this.fileIdentifier;
  }

  @Override
  public List<String> rowAt(final RowPosition position) {
    return this.rows.get(position.getIndex());
  }

  /**
   * Rows. The inner lists representing the rows can contain {@code null} values.
   */
  @Override
  public List<List<String>> rows() {
    return this.rows;
  }

  @Override
  public int rowsCount() {
    return this.rows.size();
  }
}
