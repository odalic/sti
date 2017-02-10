package uk.ac.shef.dcs.sti.core.model;

import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.ObjectMatrix2D;
import cern.colt.matrix.impl.SparseObjectMatrix1D;
import cern.colt.matrix.impl.SparseObjectMatrix2D;
import uk.ac.shef.dcs.sti.STIException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public class TAnnotation {

    protected int rows;
    protected int cols;
    protected int subjectColumn;
    protected ObjectMatrix1D headerAnnotations; //each object in the matrix is an array of TColumnHeaderAnnotation
    protected ObjectMatrix2D contentAnnotations; //each object in the matrix is an array of TCellAnnotation
    protected ObjectMatrix1D headerWarnings;
    protected ObjectMatrix2D contentWarnings;
    protected Map<RelationColumns, List<String>> columnRelationWarnings;
    protected Map<RelationColumns, Map<Integer, List<TCellCellRelationAnotation>>>
            cellcellRelations; //first key being the sub-obj column; second key is the row index
    private Map<RelationColumns, java.util.List<TColumnColumnRelationAnnotation>>
            columncolumnRelations;
    private ObjectMatrix1D statisticalAnnotations; //each object in the matrix is a TStatisticalAnnotation

    public TAnnotation(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        headerAnnotations = new SparseObjectMatrix1D(cols);
        contentAnnotations = new SparseObjectMatrix2D(rows, cols);
        headerWarnings = new SparseObjectMatrix1D(cols);
        contentWarnings = new SparseObjectMatrix2D(rows, cols);
        columnRelationWarnings = new HashMap<>();
        cellcellRelations = new HashMap<>();
        columncolumnRelations = new HashMap<>();
        statisticalAnnotations = new SparseObjectMatrix1D(cols);
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public void resetRelationAnnotations() {
        cellcellRelations.clear();
        columncolumnRelations.clear();
    }

    public void resetHeaderAnnotations() {
        headerAnnotations = new SparseObjectMatrix1D(headerAnnotations.size());
    }

    public void resetCellAnnotations() {
        contentAnnotations = new SparseObjectMatrix2D(contentAnnotations.rows(), contentAnnotations.columns());
    }

    /**
     * Target and Source must have the same dimension!!!
     *
     * @param source
     * @param target
     * @return
     */
    public static void copy(TAnnotation source, TAnnotation target) throws STIException {
        if (source.getCols() != target.getCols() || source.getRows() != target.rows)
            throw new STIException("Source and target table annotation object has different dimensions!");

        for (int col = 0; col < source.getCols(); col++) {
            TColumnHeaderAnnotation[] annotations = source.getHeaderAnnotation(col);
            if (annotations == null)
                continue;
            TColumnHeaderAnnotation[] copy = new TColumnHeaderAnnotation[annotations.length];
            for (int index = 0; index < annotations.length; index++) {
                TColumnHeaderAnnotation ann = annotations[index];
                copy[index] = TColumnHeaderAnnotation.copy(ann);
            }
            target.setHeaderAnnotation(col, copy);
        }

        for (int row = 0; row < source.getRows(); row++) {
            for (int col = 0; col < source.getCols(); col++) {
                TCellAnnotation[] annotations = source.getContentCellAnnotations(row, col);
                if (annotations == null)
                    continue;
                TCellAnnotation[] copy = new TCellAnnotation[annotations.length];
                for (int index = 0; index < annotations.length; index++)
                    copy[index] = TCellAnnotation.copy(annotations[index]);
                target.setContentCellAnnotations(row, col, copy);
            }
        }
        target.cellcellRelations = new HashMap<>(
                source.getCellcellRelations()
        );
        target.columncolumnRelations = new HashMap<>(
                source.getColumncolumnRelations()
        );

        for (int col = 0; col < source.getCols(); col++) {
            TStatisticalAnnotation annotation = source.getStatisticalAnnotation(col);
            if (annotation == null)
                continue;
            TStatisticalAnnotation copy = new TStatisticalAnnotation(annotation.getComponent(),
                annotation.getPredicateURI(), annotation.getPredicateLabel(), annotation.getScore());
            target.setStatisticalAnnotation(col, copy);
        }
    }

    public void setHeaderAnnotation(int headerCol, TColumnHeaderAnnotation[] annotations) {
        Set<TColumnHeaderAnnotation> deduplicateCheck = new HashSet<>(Arrays.asList(annotations));
        assert deduplicateCheck.size() == annotations.length;
            //assert System.err.println("duplicate header anntoations " + headerCol + ":" + deduplicateCheck);

        headerAnnotations.set(headerCol, annotations);
    }

    public TColumnHeaderAnnotation[] getHeaderAnnotation(int headerCol) {
        Object o = headerAnnotations.get(headerCol);
        if (o == null)
            return new TColumnHeaderAnnotation[0];
        TColumnHeaderAnnotation[] ha = (TColumnHeaderAnnotation[]) o;
        Arrays.sort(ha);

        return ha;
    }

    public java.util.List<TColumnHeaderAnnotation> getWinningHeaderAnnotations(int headerCol) {
        TColumnHeaderAnnotation[] annotations = getHeaderAnnotation(headerCol);

        java.util.List<TColumnHeaderAnnotation> result = new ArrayList<>();
        if (annotations == null || annotations.length == 0)
            return result;
        double prevScore = 0.0;
        for (TColumnHeaderAnnotation h : annotations) {
            if (prevScore == 0.0) {
                prevScore = h.getFinalScore();
                result.add(h);
                continue;
            }
            if (h.getFinalScore() == prevScore)
                result.add(h);
            else
                break;
        }
        return result;
    }

    public void setContentCellAnnotations(int row, int col, TCellAnnotation[] annotations) {
        Set<TCellAnnotation> deduplicateCheck = new HashSet<>(Arrays.asList(annotations));
        if (deduplicateCheck.size() != annotations.length)
            System.err.println("duplicate cell anntoations " + row + "," + col + ":" + deduplicateCheck);
        contentAnnotations.set(row, col, annotations);
    }

    public TCellAnnotation[] getContentCellAnnotations(int row, int col) {
        Object o = contentAnnotations.get(row, col);
        if (o == null)
            return new TCellAnnotation[0];
        TCellAnnotation[] ca = (TCellAnnotation[]) o;
        Arrays.sort(ca);
        return ca;
    }

    public java.util.List<TCellAnnotation> getWinningContentCellAnnotation(int row, int col) {
        TCellAnnotation[] annotations = getContentCellAnnotations(row, col);

        java.util.List<TCellAnnotation> result = new ArrayList<TCellAnnotation>();
        if (annotations == null || annotations.length == 0)
            return result;
        double prevScore = 0.0;
        for (TCellAnnotation c : annotations) {
            if (prevScore == 0.0) {
                prevScore = c.getFinalScore();
                result.add(c);
                continue;
            }
            if (c.getFinalScore() == prevScore)
                result.add(c);
            else
                break;
        }
        return result;
    }

    public void addCellCellRelation(TCellCellRelationAnotation toAdd) {
        Map<Integer, java.util.List<TCellCellRelationAnotation>> candidates //returns, key: row index; value: list of candidate relations
                = cellcellRelations.get(toAdd.getRelationColumns());
        if (candidates == null)
            candidates = new HashMap<>();
        //get the list of relations across the two columns already registered on that row
        java.util.List<TCellCellRelationAnotation> candidatesForRow = candidates.get(toAdd.getRow());
        if (candidatesForRow == null)
            candidatesForRow = new ArrayList<>();

        int existingIdentical =candidatesForRow.indexOf(toAdd);
        if (existingIdentical!=-1) {//if there is already a cellcellrelation annotation, update its score
            double newWinningMatchScore = toAdd.getWinningAttributeMatchScore();
            TCellCellRelationAnotation existing = candidatesForRow.get(existingIdentical);
            if (existing.getWinningAttributeMatchScore() < newWinningMatchScore)
                existing.setWinningAttributeMatchScore(newWinningMatchScore);
            existing.addWinningAttributes(toAdd.getWinningAttributes());
        } else
            candidatesForRow.add(toAdd);  //container for that row
        candidates.put(toAdd.getRow(), candidatesForRow); //container for that column
        cellcellRelations.put(toAdd.getRelationColumns(), candidates);
    }

    public Map<Integer, java.util.List<TCellCellRelationAnotation>> getRelationAnnotationsBetween(int subjectCol, int objectCol) {
        RelationColumns binary_key = new RelationColumns(subjectCol, objectCol);
        return cellcellRelations.get(binary_key);
    }

    public Map<RelationColumns, Map<Integer, java.util.List<TCellCellRelationAnotation>>> getCellcellRelations() {
        return cellcellRelations;
    }

    public int getSubjectColumn() {
        return subjectColumn;
    }

    public void setSubjectColumn(int subjectColumn) {
        this.subjectColumn = subjectColumn;
    }

    public Map<RelationColumns, java.util.List<TColumnColumnRelationAnnotation>> getColumncolumnRelations() {
        return columncolumnRelations;
    }

    public void addColumnColumnRelation(TColumnColumnRelationAnnotation ra) {
        java.util.List<TColumnColumnRelationAnnotation> annotations_for_columns
                = columncolumnRelations.get(ra.getRelationColumns());
        if (annotations_for_columns == null)
            annotations_for_columns = new ArrayList<>();
        annotations_for_columns.add(ra);
        columncolumnRelations.put(ra.getRelationColumns(), annotations_for_columns);
    }


    public java.util.List<TColumnColumnRelationAnnotation> getWinningRelationAnnotationsBetween(RelationColumns subobj) {
        java.util.List<TColumnColumnRelationAnnotation> candidates = columncolumnRelations.get(subobj);
        Collections.sort(candidates);

        java.util.List<TColumnColumnRelationAnnotation> result = new ArrayList<>();
        double maxScore = candidates.get(0).getFinalScore();
        for (TColumnColumnRelationAnnotation hbr : candidates) {
            if (hbr.getFinalScore() == maxScore)
                result.add(hbr);
        }
        return result;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setStatisticalAnnotation(int col, TStatisticalAnnotation annotation) {
        statisticalAnnotations.set(col, annotation);
    }

    public TStatisticalAnnotation getStatisticalAnnotation(int col) {
        Object o = statisticalAnnotations.get(col);
        if (o == null)
            return null;

        return (TStatisticalAnnotation) o;
    }

    public void addContentWarnings(int row, int column, Collection<String> newWarnings) {
        if (newWarnings == null || newWarnings.isEmpty()) {
            return;
        }

        List<String> warnings = ensureContentWarnings(row, column);

        warnings.addAll(newWarnings);
    }

    public void addContentWarning(int row, int column, String newWarning) {
        if (newWarning == null) {
            return;
        }

        List<String> warnings = ensureContentWarnings(row, column);

        warnings.add(newWarning);
    }

    @SuppressWarnings("unchecked")
    private List<String> ensureContentWarnings(int row, int column) {
        List<String> warnings;
        Object warningsObj = contentWarnings.get(row, column);

        if (warningsObj == null) {
            warnings = new ArrayList<>();
            contentWarnings.set(row, column, warnings);
        }
        else {
            warnings = (List<String>)warningsObj;
        }
        return warnings;
    }

    @SuppressWarnings("unchecked")
    public List<String> getContentWarnings(int row, int column) {
        Object warningsObj = contentWarnings.get(row, column);
        if (warningsObj == null) {
            return new ArrayList<>();
        }

        return (List<String>)warningsObj;
    }

    public void addHeaderWarnings(int headerCol, Collection<String> newWarnings) {
        if (newWarnings == null || newWarnings.isEmpty()) {
            return;
        }

        List<String> warnings = ensureHeaderWarnings(headerCol);

        warnings.addAll(newWarnings);
    }

    public void addHeaderWarning(int headerCol, String newWarning) {
        if (newWarning == null) {
            return;
        }

        List<String> warnings = ensureHeaderWarnings(headerCol);

        warnings.add(newWarning);
    }

    @SuppressWarnings("unchecked")
    private List<String> ensureHeaderWarnings(int headerCol) {
        List<String> warnings;
        Object warningsObj = headerWarnings.get(headerCol);

        if (warningsObj == null) {
            warnings = new ArrayList<>();
            headerWarnings.set(headerCol, warnings);
        }
        else {
            warnings = (List<String>)warningsObj;
        }
        return warnings;
    }

    @SuppressWarnings("unchecked")
    public List<String> getHeaderWarnings(int headerCol) {
        Object warningsObj = headerWarnings.get(headerCol);
        if (warningsObj == null) {
            return new ArrayList<>();
        }

        return (List<String>)warningsObj;
    }

    public Map<RelationColumns, List<String>> getColumnRelationWarnings() {
        return columnRelationWarnings;
    }

    public void addColumnRelationWarnings(RelationColumns columns, Collection<String> newWarnings) {
        List<String> warnings = columnRelationWarnings.computeIfAbsent(columns, k -> new ArrayList<>());
        warnings.addAll(newWarnings);
    }
}
