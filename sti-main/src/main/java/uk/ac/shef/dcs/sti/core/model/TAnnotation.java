package uk.ac.shef.dcs.sti.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.ObjectMatrix2D;
import cern.colt.matrix.impl.SparseObjectMatrix1D;
import cern.colt.matrix.impl.SparseObjectMatrix2D;
import uk.ac.shef.dcs.sti.STIException;

/**
 */
public class TAnnotation {

  /**
   * Target and Source must have the same dimension!!!
   *
   * @param source
   * @param target
   * @return
   */
  public static void copy(final TAnnotation source, final TAnnotation target) throws STIException {
    if ((source.getCols() != target.getCols()) || (source.getRows() != target.rows)) {
      throw new STIException("Source and target table annotation object has different dimensions!");
    }

    for (int col = 0; col < source.getCols(); col++) {
      final TColumnHeaderAnnotation[] annotations = source.getHeaderAnnotation(col);
      if (annotations == null) {
        continue;
      }
      final TColumnHeaderAnnotation[] copy = new TColumnHeaderAnnotation[annotations.length];
      for (int index = 0; index < annotations.length; index++) {
        final TColumnHeaderAnnotation ann = annotations[index];
        copy[index] = TColumnHeaderAnnotation.copy(ann);
      }
      target.setHeaderAnnotation(col, copy);
    }

    for (int row = 0; row < source.getRows(); row++) {
      for (int col = 0; col < source.getCols(); col++) {
        final TCellAnnotation[] annotations = source.getContentCellAnnotations(row, col);
        if (annotations == null) {
          continue;
        }
        final TCellAnnotation[] copy = new TCellAnnotation[annotations.length];
        for (int index = 0; index < annotations.length; index++) {
          copy[index] = TCellAnnotation.copy(annotations[index]);
        }
        target.setContentCellAnnotations(row, col, copy);
      }
    }
    target.cellcellRelations = new HashMap<>(source.getCellcellRelations());
    target.columncolumnRelations = new HashMap<>(source.getColumncolumnRelations());

    for (int col = 0; col < source.getCols(); col++) {
      final TStatisticalAnnotation annotation = source.getStatisticalAnnotation(col);
      if (annotation == null) {
        continue;
      }
      final TStatisticalAnnotation copy = new TStatisticalAnnotation(annotation.getComponent(),
          annotation.getPredicateURI(), annotation.getPredicateLabel(), annotation.getScore());
      target.setStatisticalAnnotation(col, copy);
    }

    for (int col = 0; col < source.getCols(); col++) {
      final TColumnProcessingAnnotation annotation = source.getColumnProcessingAnnotation(col);
      if (annotation == null) {
        continue;
      }
      final TColumnProcessingAnnotation copy =
          new TColumnProcessingAnnotation(annotation.getProcessingType());
      target.setColumnProcessingAnnotation(col, copy);
    }
  }

  protected int rows;
  protected int cols;
  protected Set<Integer> subjectColumns;
  protected ObjectMatrix1D headerAnnotations; // each object in the matrix is an array of
                                              // TColumnHeaderAnnotation
  protected ObjectMatrix2D contentAnnotations; // each object in the matrix is an array of
                                               // TCellAnnotation
  protected ObjectMatrix1D headerWarnings;
  protected ObjectMatrix2D contentWarnings;
  protected Map<RelationColumns, List<String>> columnRelationWarnings;
  protected Map<RelationColumns, Map<Integer, List<TCellCellRelationAnotation>>> cellcellRelations; // first
                                                                                                    // key
                                                                                                    // being
                                                                                                    // the
                                                                                                    // sub-obj
                                                                                                    // column;
                                                                                                    // second
                                                                                                    // key
                                                                                                    // is
                                                                                                    // the
                                                                                                    // row
                                                                                                    // index
  private Map<RelationColumns, java.util.List<TColumnColumnRelationAnnotation>> columncolumnRelations;
  private final ObjectMatrix1D statisticalAnnotations; // each object in the matrix is a
                                                       // TStatisticalAnnotation

  private final ObjectMatrix1D columnProcessingAnnotations; // each object in the matrix is a
                                                            // TColumnProcessingAnnotation

  public TAnnotation(final int rows, final int cols) {
    this.rows = rows;
    this.cols = cols;
    this.headerAnnotations = new SparseObjectMatrix1D(cols);
    this.contentAnnotations = new SparseObjectMatrix2D(rows, cols);
    this.headerWarnings = new SparseObjectMatrix1D(cols);
    this.contentWarnings = new SparseObjectMatrix2D(rows, cols);
    this.columnRelationWarnings = new HashMap<>();
    this.cellcellRelations = new HashMap<>();
    this.columncolumnRelations = new HashMap<>();
    this.statisticalAnnotations = new SparseObjectMatrix1D(cols);
    this.columnProcessingAnnotations = new SparseObjectMatrix1D(cols);
    this.subjectColumns = new HashSet<>();
  }

  public void addCellCellRelation(final TCellCellRelationAnotation toAdd) {
    Map<Integer, java.util.List<TCellCellRelationAnotation>> candidates // returns, key: row index;
                                                                        // value: list of candidate
                                                                        // relations
        = this.cellcellRelations.get(toAdd.getRelationColumns());
    if (candidates == null) {
      candidates = new HashMap<>();
    }
    // get the list of relations across the two columns already registered on that row
    java.util.List<TCellCellRelationAnotation> candidatesForRow = candidates.get(toAdd.getRow());
    if (candidatesForRow == null) {
      candidatesForRow = new ArrayList<>();
    }

    final int existingIdentical = candidatesForRow.indexOf(toAdd);
    if (existingIdentical != -1) {// if there is already a cellcellrelation annotation, update its
                                  // score
      final double newWinningMatchScore = toAdd.getWinningAttributeMatchScore();
      final TCellCellRelationAnotation existing = candidatesForRow.get(existingIdentical);
      if (existing.getWinningAttributeMatchScore() < newWinningMatchScore) {
        existing.setWinningAttributeMatchScore(newWinningMatchScore);
      }
      existing.addWinningAttributes(toAdd.getWinningAttributes());
    } else {
      candidatesForRow.add(toAdd); // container for that row
    }
    candidates.put(toAdd.getRow(), candidatesForRow); // container for that column
    this.cellcellRelations.put(toAdd.getRelationColumns(), candidates);
  }

  public void addColumnColumnRelation(final TColumnColumnRelationAnnotation ra) {
    java.util.List<TColumnColumnRelationAnnotation> annotations_for_columns =
        this.columncolumnRelations.get(ra.getRelationColumns());
    if (annotations_for_columns == null) {
      annotations_for_columns = new ArrayList<>();
    }
    annotations_for_columns.add(ra);
    this.columncolumnRelations.put(ra.getRelationColumns(), annotations_for_columns);
  }

  public void addColumnRelationWarnings(final RelationColumns columns,
      final Collection<String> newWarnings) {
    final List<String> warnings =
        this.columnRelationWarnings.computeIfAbsent(columns, k -> new ArrayList<>());
    warnings.addAll(newWarnings);
  }

  public void addContentWarning(final int row, final int column, final String newWarning) {
    if (newWarning == null) {
      return;
    }

    final List<String> warnings = ensureContentWarnings(row, column);

    warnings.add(newWarning);
  }

  public void addContentWarnings(final int row, final int column,
      final Collection<String> newWarnings) {
    if ((newWarnings == null) || newWarnings.isEmpty()) {
      return;
    }

    final List<String> warnings = ensureContentWarnings(row, column);

    warnings.addAll(newWarnings);
  }

  public void addHeaderWarning(final int headerCol, final String newWarning) {
    if (newWarning == null) {
      return;
    }

    final List<String> warnings = ensureHeaderWarnings(headerCol);

    warnings.add(newWarning);
  }

  public void addHeaderWarnings(final int headerCol, final Collection<String> newWarnings) {
    if ((newWarnings == null) || newWarnings.isEmpty()) {
      return;
    }

    final List<String> warnings = ensureHeaderWarnings(headerCol);

    warnings.addAll(newWarnings);
  }

  @SuppressWarnings("unchecked")
  private List<String> ensureContentWarnings(final int row, final int column) {
    List<String> warnings;
    final Object warningsObj = this.contentWarnings.get(row, column);

    if (warningsObj == null) {
      warnings = new ArrayList<>();
      this.contentWarnings.set(row, column, warnings);
    } else {
      warnings = (List<String>) warningsObj;
    }
    return warnings;
  }

  @SuppressWarnings("unchecked")
  private List<String> ensureHeaderWarnings(final int headerCol) {
    List<String> warnings;
    final Object warningsObj = this.headerWarnings.get(headerCol);

    if (warningsObj == null) {
      warnings = new ArrayList<>();
      this.headerWarnings.set(headerCol, warnings);
    } else {
      warnings = (List<String>) warningsObj;
    }
    return warnings;
  }

  public Map<RelationColumns, Map<Integer, java.util.List<TCellCellRelationAnotation>>> getCellcellRelations() {
    return this.cellcellRelations;
  }

  public int getCols() {
    return this.cols;
  }

  public Map<RelationColumns, java.util.List<TColumnColumnRelationAnnotation>> getColumncolumnRelations() {
    return this.columncolumnRelations;
  }

  public TColumnProcessingAnnotation getColumnProcessingAnnotation(final int col) {
    final Object o = this.columnProcessingAnnotations.get(col);
    if (o == null) {
      return null;
    }

    return (TColumnProcessingAnnotation) o;
  }

  public Map<RelationColumns, List<String>> getColumnRelationWarnings() {
    return this.columnRelationWarnings;
  }

  public TCellAnnotation[] getContentCellAnnotations(final int row, final int col) {
    final Object o = this.contentAnnotations.get(row, col);
    if (o == null) {
      return new TCellAnnotation[0];
    }
    final TCellAnnotation[] ca = (TCellAnnotation[]) o;
    Arrays.sort(ca);
    return ca;
  }

  @SuppressWarnings("unchecked")
  public List<String> getContentWarnings(final int row, final int column) {
    final Object warningsObj = this.contentWarnings.get(row, column);
    if (warningsObj == null) {
      return new ArrayList<>();
    }

    return (List<String>) warningsObj;
  }

  public TColumnHeaderAnnotation[] getHeaderAnnotation(final int headerCol) {
    final Object o = this.headerAnnotations.get(headerCol);
    if (o == null) {
      return new TColumnHeaderAnnotation[0];
    }
    final TColumnHeaderAnnotation[] ha = (TColumnHeaderAnnotation[]) o;
    Arrays.sort(ha);

    return ha;
  }

  @SuppressWarnings("unchecked")
  public List<String> getHeaderWarnings(final int headerCol) {
    final Object warningsObj = this.headerWarnings.get(headerCol);
    if (warningsObj == null) {
      return new ArrayList<>();
    }

    return (List<String>) warningsObj;
  }


  public Map<Integer, java.util.List<TCellCellRelationAnotation>> getRelationAnnotationsBetween(
      final int subjectCol, final int objectCol) {
    final RelationColumns binary_key = new RelationColumns(subjectCol, objectCol);
    return this.cellcellRelations.get(binary_key);
  }

  public int getRows() {
    return this.rows;
  }

  public TStatisticalAnnotation getStatisticalAnnotation(final int col) {
    final Object o = this.statisticalAnnotations.get(col);
    if (o == null) {
      return null;
    }

    return (TStatisticalAnnotation) o;
  }

  public java.util.List<TCellAnnotation> getWinningContentCellAnnotation(final int row,
      final int col) {
    final TCellAnnotation[] annotations = getContentCellAnnotations(row, col);

    final java.util.List<TCellAnnotation> result = new ArrayList<TCellAnnotation>();
    if ((annotations == null) || (annotations.length == 0)) {
      return result;
    }
    double prevScore = 0.0;
    for (final TCellAnnotation c : annotations) {
      if (prevScore == 0.0) {
        prevScore = c.getFinalScore();
        result.add(c);
        continue;
      }
      if (c.getFinalScore() == prevScore) {
        result.add(c);
      } else {
        break;
      }
    }
    return result;
  }

  public java.util.List<TColumnHeaderAnnotation> getWinningHeaderAnnotations(final int headerCol) {
    final TColumnHeaderAnnotation[] annotations = getHeaderAnnotation(headerCol);

    final java.util.List<TColumnHeaderAnnotation> result = new ArrayList<>();
    if ((annotations == null) || (annotations.length == 0)) {
      return result;
    }
    double prevScore = 0.0;
    for (final TColumnHeaderAnnotation h : annotations) {
      if (prevScore == 0.0) {
        prevScore = h.getFinalScore();
        result.add(h);
        continue;
      }
      if (h.getFinalScore() == prevScore) {
        result.add(h);
      } else {
        break;
      }
    }
    return result;
  }

  public java.util.List<TColumnColumnRelationAnnotation> getWinningRelationAnnotationsBetween(
      final RelationColumns subobj) {
    final java.util.List<TColumnColumnRelationAnnotation> candidates =
        this.columncolumnRelations.get(subobj);
    Collections.sort(candidates);

    final java.util.List<TColumnColumnRelationAnnotation> result = new ArrayList<>();
    final double maxScore = candidates.get(0).getFinalScore();
    for (final TColumnColumnRelationAnnotation hbr : candidates) {
      if (hbr.getFinalScore() == maxScore) {
        result.add(hbr);
      }
    }
    return result;
  }

  public void resetCellAnnotations() {
    this.contentAnnotations =
        new SparseObjectMatrix2D(this.contentAnnotations.rows(), this.contentAnnotations.columns());
  }

  public void resetHeaderAnnotations() {
    this.headerAnnotations = new SparseObjectMatrix1D(this.headerAnnotations.size());
  }

  public void resetRelationAnnotations() {
    this.cellcellRelations.clear();
    this.columncolumnRelations.clear();
  }

  public void setColumnProcessingAnnotation(final int col,
      final TColumnProcessingAnnotation annotation) {
    this.columnProcessingAnnotations.set(col, annotation);
  }

  public void setContentCellAnnotations(final int row, final int col,
      final TCellAnnotation[] annotations) {
    final Set<TCellAnnotation> deduplicateCheck = new HashSet<>(Arrays.asList(annotations));
    if (deduplicateCheck.size() != annotations.length) {
      System.err.println("duplicate cell anntoations " + row + "," + col + ":" + deduplicateCheck);
    }
    this.contentAnnotations.set(row, col, annotations);
  }

  public void setHeaderAnnotation(final int headerCol,
      final TColumnHeaderAnnotation[] annotations) {
    final Set<TColumnHeaderAnnotation> deduplicateCheck = new HashSet<>(Arrays.asList(annotations));
    assert deduplicateCheck.size() == annotations.length;
    // assert System.err.println("duplicate header anntoations " + headerCol + ":" +
    // deduplicateCheck);

    this.headerAnnotations.set(headerCol, annotations);
  }

  public void setRows(final int rows) {
    this.rows = rows;
  }

  public void setStatisticalAnnotation(final int col, final TStatisticalAnnotation annotation) {
    this.statisticalAnnotations.set(col, annotation);
  }

  public void setSubjectColumns(final Set<Integer> subjectColumns) {
    this.subjectColumns = subjectColumns;
  }

  public Set<Integer> getSubjectColumns() {
    return this.subjectColumns;
  }
}
