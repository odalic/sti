package uk.ac.shef.dcs.oak.sti.rep;

import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.ObjectMatrix2D;
import cern.colt.matrix.impl.SparseObjectMatrix1D;
import cern.colt.matrix.impl.SparseObjectMatrix2D;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.*;

/**
 * An LTable always has horizontally related columns. First row always headers
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 01/10/12
 * Time: 15:06
 */
public class LTable implements Serializable {
    private String sourceId;

    private String tableId;

    private String tableXPath;

    private Map<Integer, String> rowXPaths;

    private ObjectMatrix1D headers; //an object can only be a LTableColumnHeader object

    private ObjectMatrix2D contents;//an object can only be a LTableContentCell object

    private int rows; //# of rows in the table (excluding header)

    private int cols; //# of columns in the table

    //private List<CellBinaryRelationAnnotation> relations = new ArrayList<CellBinaryRelationAnnotation>();
    private List<LTableContext> contexts = new ArrayList<LTableContext>();

    private LTableAnnotation tableAnnotations;

    private Charset encoding;

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("Table: SourceID " + sourceId + ",\n");
        res.append("Table: TableID " + tableId + ",\n");
        res.append("Table: TableXPath " + tableXPath + ",\n");
        res.append("Table: RowXPaths " + rowXPaths + ",\n");
        res.append("Table: rows " + rows + ",\n");
        res.append("Table: cols " + cols + ",\n");
        res.append("Table: headers " + headers + ",\n");
        res.append("Table: contents " + contents + ",\n");

        return res.toString();
    }

    public LTable(String id, String sourceId, int rows, int cols) {
        this.tableId = id;
        this.sourceId = sourceId;

        this.rows = rows;
        this.cols = cols;
        contents = new SparseObjectMatrix2D(rows, cols);
        headers = new SparseObjectMatrix1D(cols);

        rowXPaths = new LinkedHashMap<Integer, String>();
        tableAnnotations = new LTableAnnotation(rows, cols);
    }

    public LTable(String id, String sourceId) {
        this.tableId = id;
        this.sourceId = sourceId;

        rowXPaths = new LinkedHashMap<Integer, String>();
    }

    public int getNumRows() {
        return rows;
    }

    public void setNumRows(int row) {
        this.rows = row;
    }

    public int getNumCols() {
        return cols;
    }

    public void setNumCols(int col) {
        this.cols = col;
    }

    //single header/cell
    public void setColumnHeader(int c, LTableColumnHeader header) {
        headers.set(c, header);
    }

    public LTableColumnHeader getColumnHeader(int c) {
        Object o = headers.get(c);
        if (o == null)
            return null;

        return (LTableColumnHeader) o;
    }

    public void setContentCell(int r, int c, LTableContentCell cell) {
        contents.set(r, c, cell);
    }

    public LTableContentCell getContentCell(int r, int c) {
        return (LTableContentCell) contents.get(r, c);
    }

    //headers and content cells;
    protected ObjectMatrix1D getTableHeaders() {
        return headers;
    }

    protected ObjectMatrix2D getContentCells() {
        ObjectMatrix2D contentCells = contents.viewPart(1, 0, contents.rows() - 1, contents.columns());
        return contentCells;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public List<LTableContext> getContexts() {
        return contexts;
    }

    public void addContext(LTableContext context) {
        contexts.add(context);
    }

    public boolean equals(Object o) {
        if (o instanceof LTable) {
            LTable t = (LTable) o;
            return t.getTableId().equals(getTableId()) && t.getSourceId().equals(getSourceId());
        }
        return false;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTableXPath() {
        return tableXPath;
    }

    public void setTableXPath(String tableXPath) {
        this.tableXPath = tableXPath;
    }

    public Map<Integer, String> getRowXPaths() {
        return rowXPaths;
    }

    public LTableAnnotation getTableAnnotations() {
        return tableAnnotations;
    }

    public void setTableAnnotations(LTableAnnotation tableAnnotations) {
        this.tableAnnotations = tableAnnotations;
    }

    public int size() {
        return contents.size();
    }

    public int getNumHeaders() {
        return headers.size();
    }

    public void setEncoding(Charset encoding) {
        this.encoding = encoding;

    }

    public Charset getEncoding() {
        return encoding;
    }
}
