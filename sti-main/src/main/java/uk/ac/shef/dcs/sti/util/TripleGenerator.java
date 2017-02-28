package uk.ac.shef.dcs.sti.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.core.model.RelationColumns;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnColumnRelationAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeader;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.model.TableTriple;

/**
 */
public class TripleGenerator {

  private final String kbNamespace;
  private final String defaultNamespace;

  public TripleGenerator(final String kbNamespace, final String dummyNamespace) {
    this.kbNamespace = kbNamespace;
    this.defaultNamespace = dummyNamespace;
  }

  public List<TableTriple> generate_newTriples(final TAnnotation tab_annotation,
      final Table table) {
    final List<TableTriple> result = new ArrayList<>();

    // column typing instances
    for (int col = 0; col < table.getNumCols(); col++) {
      final TColumnHeader header = table.getColumnHeader(col);

      final List<TColumnHeaderAnnotation> bestHeaderAnnotations =
          tab_annotation.getWinningHeaderAnnotations(col);
      if (bestHeaderAnnotations.size() == 0) {
        continue;
      }


      for (int row = 0; row < table.getNumRows(); row++) {
        for (final TColumnHeaderAnnotation final_type_for_the_column : bestHeaderAnnotations) {

          /*
           * if (final_type_for_the_column.getSupportingRows().contains(row)) continue;
           */
          final TCell tcc = table.getContentCell(row, col);
          final TCellAnnotation[] cell_annotations =
              tab_annotation.getContentCellAnnotations(row, col);
          if ((cell_annotations == null) || (cell_annotations.length == 0)) {
            continue;
          }

          final TCellAnnotation final_cell_annotation = cell_annotations[0];
          final Entity entity = final_cell_annotation.getAnnotation();

          // new triple
          final TableTriple ltt = new TableTriple();
          ltt.setSubject_position(new int[] {row, col});
          ltt.setSubject(tcc.getText());
          ltt.setSubject_annotation(this.kbNamespace + entity.getId());
          ltt.setObject(header.getHeaderText());
          ltt.setObject_annotation(
              this.kbNamespace + final_type_for_the_column.getAnnotation().getId());
          ltt.setObject_position(new int[] {-1, -1});
          ltt.setRelation_annotation("rdf:type");
          result.add(ltt);
        }
      }
    }

    // across column relations at each row
    final List<Integer> related_columns_with_subject = new ArrayList<Integer>();
    int main_subject_column = 0;
    final Map<RelationColumns, List<TColumnColumnRelationAnnotation>> relations_across_columns =
        tab_annotation.getColumncolumnRelations();
    for (final Map.Entry<RelationColumns, List<TColumnColumnRelationAnnotation>> entry : relations_across_columns
        .entrySet()) {
      final RelationColumns the_two_columns = entry.getKey();
      final int subCol = the_two_columns.getSubjectCol();
      final int objCol = the_two_columns.getObjectCol();
      related_columns_with_subject.add(objCol);
      main_subject_column = subCol;

      Collections.sort(entry.getValue());
      final TColumnColumnRelationAnnotation relation_annotation = entry.getValue().get(0);

      for (int row = 0; row < table.getNumRows(); row++) {
        if (relation_annotation.getSupportingRows().contains(row)) {
          continue;
        }

        table.getContentCell(row, subCol);
        final TCell object_cell = table.getContentCell(row, objCol);
        final TCellAnnotation[] subject_cell_annotations =
            tab_annotation.getContentCellAnnotations(row, subCol);
        if ((subject_cell_annotations == null) || (subject_cell_annotations.length == 0)) {
          continue;
        }
        final TCellAnnotation final_subject_cell_annotation = subject_cell_annotations[0];
        final TCellAnnotation[] object_cell_annotations =
            tab_annotation.getContentCellAnnotations(row, objCol);
        final TCellAnnotation final_object_cell_annotation =
            (object_cell_annotations == null) || (object_cell_annotations.length == 0) ? null
                : object_cell_annotations[0];

        final TableTriple triple = new TableTriple();
        triple.setSubject_position(new int[] {row, subCol});
        triple.setSubject(final_subject_cell_annotation.getTerm());
        triple.setSubject_annotation(
            this.kbNamespace + final_subject_cell_annotation.getAnnotation().getId());

        triple.setObject_position(new int[] {row, objCol});
        if (final_object_cell_annotation != null) {
          triple.setObject_annotation(
              this.kbNamespace + final_object_cell_annotation.getAnnotation().getId());
          triple.setObject(final_object_cell_annotation.getTerm());
        } else {
          triple.setObject_annotation("'" + object_cell.getText() + "'");
          triple.setObject(object_cell.getText());
        }
        triple.setRelation_annotation(this.kbNamespace + relation_annotation.getRelationURI());
        result.add(triple);

      }
    }

    // remaining columns with subject column create dummy relations
    for (int col = 0; col < table.getNumCols(); col++) {
      if ((col == main_subject_column) || related_columns_with_subject.contains(col)) {
        continue;
      }

      final TColumnHeader header = table.getColumnHeader(col);
      if ((header != null) && (header.getTypes() != null)) {
        if (header.getTypes().get(0).getType().equals(DataTypeClassifier.DataType.ORDERED_NUMBER)) {
          continue;
        }
      } else {
        continue;
      }

      for (int row = 0; row < table.getNumRows(); row++) {
        table.getContentCell(row, main_subject_column);
        final TCell object_cell = table.getContentCell(row, col);
        final TCellAnnotation[] subject_cell_annotations =
            tab_annotation.getContentCellAnnotations(row, main_subject_column);
        if ((subject_cell_annotations == null) || (subject_cell_annotations.length == 0)) {
          continue;
        }
        final TCellAnnotation final_subject_cell_annotation = subject_cell_annotations[0];

        final TableTriple triple = new TableTriple();
        triple.setSubject_position(new int[] {row, main_subject_column});
        triple.setSubject(final_subject_cell_annotation.getTerm());
        triple.setSubject_annotation(
            this.kbNamespace + final_subject_cell_annotation.getAnnotation().getId());

        triple.setObject_position(new int[] {row, col});
        triple.setObject(object_cell.getText());
        triple.setObject_annotation("'" + object_cell.getText() + "'");
        triple.setRelation_annotation(this.defaultNamespace + "/" + header.getHeaderText());
        result.add(triple);
      }
    }

    return result;
  }
}
