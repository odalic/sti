package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.ObjectMatrix2D;
import cern.colt.matrix.impl.SparseObjectMatrix1D;
import cern.colt.matrix.impl.SparseObjectMatrix2D;


/**
 * An Table always has horizontally related columns. First row always headers
 *
 *
 *
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk) Date: 01/10/12 Time: 15:06
 */
public class Table implements Serializable {
  private static final long serialVersionUID = -3422675814777405913L;
  private String sourceId;
  private String tableId;

  private String tableXPath;
  private final Map<Integer, String> rowXPaths;

  private final ObjectMatrix1D headers; // an object can only be a TColumnHeader object
  private final ObjectMatrix2D contents;// an object can only be a TCell object


  private int rows; // # of rows in the table (excluding header)
  private int cols; // # of columns in the table

  // private List<CellBinaryRelationAnnotation> relations = new
  // ArrayList<CellBinaryRelationAnnotation>();
  private final java.util.List<TContext> contexts = new ArrayList<>();

  private TAnnotation tableAnnotations;


  public Table(final String id, final String sourceId, final int rows, final int cols) {
    this.tableId = id;
    this.sourceId = sourceId;

    this.rows = rows;
    this.cols = cols;
    this.contents = new SparseObjectMatrix2D(rows, cols);
    this.headers = new SparseObjectMatrix1D(cols);

    this.rowXPaths = new LinkedHashMap<Integer, String>();
    this.tableAnnotations = new TAnnotation(rows, cols);
  }

  public void addContext(final TContext context) {
    this.contexts.add(context);
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof Table) {
      final Table t = (Table) o;
      return t.getTableId().equals(getTableId()) && t.getSourceId().equals(getSourceId());
    }
    return false;
  }

  public TColumnHeader getColumnHeader(final int c) {
    final Object o = this.headers.get(c);
    if (o == null) {
      return null;
    }

    return (TColumnHeader) o;
  }

  public TCell getContentCell(final int r, final int c) {
    return (TCell) this.contents.get(r, c);
  }

  public java.util.List<TContext> getContexts() {
    return this.contexts;
  }

  public int getNumCols() {
    return this.cols;
  }

  public int getNumHeaders() {
    return this.headers.size();
  }

  public int getNumRows() {
    return this.rows;
  }


  // headers and content cells;

  public Map<Integer, String> getRowXPaths() {
    return this.rowXPaths;
  }

  public String getSourceId() {
    return this.sourceId;
  }


  public TAnnotation getTableAnnotations() {
    return this.tableAnnotations;
  }

  public String getTableId() {
    return this.tableId;
  }

  public String getTableXPath() {
    return this.tableXPath;
  }

  // single header/cell
  public void setColumnHeader(final int c, final TColumnHeader header) {
    this.headers.set(c, header);
  }

  public void setContentCell(final int r, final int c, final TCell cell) {
    this.contents.set(r, c, cell);
  }

  public void setNumCols(final int col) {
    this.cols = col;
  }

  public void setNumRows(final int row) {
    this.rows = row;
  }

  public void setSourceId(final String sourceId) {
    this.sourceId = sourceId;
  }

  public void setTableAnnotations(final TAnnotation tableAnnotations) {
    this.tableAnnotations = tableAnnotations;
  }


  public void setTableId(final String tableId) {
    this.tableId = tableId;
  }

  public void setTableXPath(final String tableXPath) {
    this.tableXPath = tableXPath;
  }

  public int size() {
    return this.contents.size();
  }

  @Override
  public String toString() {
    return getSourceId() + "," + getTableId();
  }
}
