package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.kbproxy.ProxyException;
import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.RelationColumns;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TCellCellRelationAnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnColumnRelationAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

/**
 * this simply chooses column type based on relations' expected types
 */
public class LiteralColumnTaggerImpl implements LiteralColumnTagger {
  private static final Logger LOG =
      LoggerFactory.getLogger(LiteralColumnTaggerImpl.class.getName());
  private int[] ignoreColumns;

  public LiteralColumnTaggerImpl() {

  }

  public LiteralColumnTaggerImpl(final int... ignoreColumns) {
    this.ignoreColumns = ignoreColumns;

  }

  @Override
  public void annotate(final Table table, final TAnnotation annotations, final Integer... neColumns)
      throws ProxyException {
    annotate(table, annotations, new Constraints(), neColumns);
  }

  @Override
  public void annotate(final Table table, final TAnnotation annotations,
      final Constraints constraints, final Integer... neColumns) throws ProxyException {
    // for each column that has a relation with the subject column, infer its type
    final Map<RelationColumns, Map<Integer, List<TCellCellRelationAnotation>>> relationAnnotations =
        annotations.getCellcellRelations();

    // LOG.info("\t>> Annotating literal columns");
    for (final Map.Entry<RelationColumns, Map<Integer, List<TCellCellRelationAnotation>>> e : relationAnnotations
        .entrySet()) {
      final RelationColumns subcol_objcol = e.getKey();
      if (ignoreColumn(subcol_objcol.getObjectCol())) {
        continue;
      }

      LOG.info("\t\t>> object column= " + subcol_objcol.getObjectCol());
      boolean skip = false;

      // check if the object column is an ne column, and whether that ne column is already annotated
      // if so we do not need to annotate this column, we just need to skip it
      for (final int i : neColumns) {
        final boolean isColumn_acronym_or_code =
            table.getColumnHeader(i).getFeature().isAcronymColumn();
        if ((i == subcol_objcol.getObjectCol()) && !isColumn_acronym_or_code) {
          if ((annotations.getHeaderAnnotation(i) != null && annotations.getHeaderAnnotation(i).length > 0) ||
              (constraints.getClassifications().stream().anyMatch(ec -> (ec.getPosition().getIndex() == i)))) {
            skip = true;
            break;
          }
        }
      }
      if (skip) {
        LOG.debug(
            "\t\t>> skipped object column (possibly NE column) " + subcol_objcol.getObjectCol());
        continue;
      }

      final List<TColumnHeaderAnnotation> candidates = new ArrayList<>();
      final List<TColumnColumnRelationAnnotation> relations =
          annotations.getColumncolumnRelations().get(subcol_objcol); // get the relation annotations
                                                                     // between subject col and this
                                                                     // column
      for (final TColumnColumnRelationAnnotation relation : relations) {
        // we simply create a new clazz using the relation's uri and label
        final TColumnHeaderAnnotation hAnn = new TColumnHeaderAnnotation(
            table.getColumnHeader(subcol_objcol.getObjectCol()).getHeaderText(),
            new Clazz(relation.getRelationURI(), relation.getRelationLabel()),
            relation.getFinalScore());
        if (!candidates.contains(hAnn)) {
          candidates.add(hAnn);
        }
      }

      final List<TColumnHeaderAnnotation> sorted = new ArrayList<>(candidates);
      Collections.sort(sorted);
      annotations.setHeaderAnnotation(subcol_objcol.getObjectCol(),
          sorted.toArray(new TColumnHeaderAnnotation[0]));
    }

  }

  private boolean ignoreColumn(final Integer i) {
    if (i != null) {
      for (final int a : this.ignoreColumns) {
        if (a == i) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void setIgnoreColumns(final int... ignoreCols) {
    this.ignoreColumns = ignoreCols;
  }

}
