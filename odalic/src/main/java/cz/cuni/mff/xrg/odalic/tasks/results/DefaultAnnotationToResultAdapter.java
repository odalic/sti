package cz.cuni.mff.xrg.odalic.tasks.results;

import cz.cuni.mff.xrg.odalic.api.rest.values.ComponentTypeValue;
import cz.cuni.mff.xrg.odalic.entities.EntitiesFactory;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Score;
import cz.cuni.mff.xrg.odalic.tasks.annotations.StatisticalAnnotation;
import cz.cuni.mff.xrg.odalic.util.Arrays;
import cz.cuni.mff.xrg.odalic.util.Lists;
import cz.cuni.mff.xrg.odalic.util.Maps;
import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.sti.core.model.RelationColumns;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnColumnRelationAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.TStatisticalAnnotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import org.apache.jena.ext.com.google.common.collect.ImmutableList;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 * This implementation of {@link AnnotationToResultAdapter} simply merges the annotations done with
 * different knowledge bases.
 * 
 * @author Jan Váňa
 * @author Václav Brodec
 * @author Josef Janoušek
 *
 */
@Immutable
public class DefaultAnnotationToResultAdapter implements AnnotationToResultAdapter {

  private EntitiesFactory entitiesFactory;

  public DefaultAnnotationToResultAdapter(final EntitiesFactory entitesFactory) {
    Preconditions.checkNotNull(entitesFactory);

    this.entitiesFactory = entitesFactory;
  }

  /**
   * This implementation demands that the subject columns recognized in the annotations are the
   * same.
   * 
   * @see cz.cuni.mff.xrg.odalic.tasks.results.AnnotationToResultAdapter#toResult(Map,
   *      KnowledgeBase)
   */
  @Override
  public Result toResult(
      Map<? extends KnowledgeBase, ? extends TAnnotation> basesToTableAnnotations) {
    Preconditions.checkArgument(!basesToTableAnnotations.isEmpty());

    // Extract subject column positions.
    final Map<KnowledgeBase, ColumnPosition> subjectColumnPositions =
        extractSubjectColumnPositions(basesToTableAnnotations);

    // Merge annotations.
    final Iterator<? extends Map.Entry<? extends KnowledgeBase, ? extends TAnnotation>> entrySetIterator =
        initializeEntrySetIterator(basesToTableAnnotations);

    // Process the first entry to initialize working structures for the merges.
    final Map.Entry<? extends KnowledgeBase, ? extends TAnnotation> firstEntry =
        entrySetIterator.next();
    final KnowledgeBase firstKnowledgeBase = firstEntry.getKey();
    final TAnnotation firstTableAnnotation = firstEntry.getValue();

    final List<HeaderAnnotation> mergedHeaderAnnotations =
        convertColumnAnnotations(firstKnowledgeBase, firstTableAnnotation);
    final CellAnnotation[][] mergedCellAnnotations =
        convertCellAnnotations(firstKnowledgeBase, firstTableAnnotation);
    final Map<ColumnRelationPosition, ColumnRelationAnnotation> mergedColumnRelations =
        convertColumnRelations(firstKnowledgeBase, firstTableAnnotation);
    final List<StatisticalAnnotation> mergedStatisticalAnnotations =
        convertStatisticalAnnotations(firstKnowledgeBase, firstTableAnnotation);

    // Process the rest.
    processTheRest(entrySetIterator, mergedHeaderAnnotations, mergedCellAnnotations,
        mergedColumnRelations, mergedStatisticalAnnotations);

    return new Result(subjectColumnPositions, mergedHeaderAnnotations, mergedCellAnnotations,
        mergedColumnRelations, mergedStatisticalAnnotations, ImmutableList.of()); // TODO: Implement warnings.
  }

  private static Map<KnowledgeBase, ColumnPosition> extractSubjectColumnPositions(
      Map<? extends KnowledgeBase, ? extends TAnnotation> basesToTableAnnotations) {
    final ImmutableMap.Builder<KnowledgeBase, ColumnPosition> subjectColumnPositionsBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends TAnnotation> annotationEntry : basesToTableAnnotations
        .entrySet()) {
      final KnowledgeBase base = annotationEntry.getKey();
      final ColumnPosition subjectColumnPosition =
          extractSubjectColumnPosition(annotationEntry.getValue());

      subjectColumnPositionsBuilder.put(base, subjectColumnPosition);
    }
    return subjectColumnPositionsBuilder.build();
  }

  private static Iterator<? extends Map.Entry<? extends KnowledgeBase, ? extends TAnnotation>> initializeEntrySetIterator(
      Map<? extends KnowledgeBase, ? extends TAnnotation> basesToTableAnnotations) {
    final Set<? extends Map.Entry<? extends KnowledgeBase, ? extends TAnnotation>> entrySet =
        basesToTableAnnotations.entrySet();
    final Iterator<? extends Map.Entry<? extends KnowledgeBase, ? extends TAnnotation>> entrySetIterator =
        entrySet.iterator();
    return entrySetIterator;
  }

  private void processTheRest(
      final Iterator<? extends Map.Entry<? extends KnowledgeBase, ? extends TAnnotation>> entrySetIterator,
      final List<HeaderAnnotation> mergedHeaderAnnotations,
      final CellAnnotation[][] mergedCellAnnotations,
      final Map<ColumnRelationPosition, ColumnRelationAnnotation> mergedColumnRelations,
      final List<StatisticalAnnotation> mergedStatisticalAnnotations) {
    while (entrySetIterator.hasNext()) {
      final Map.Entry<? extends KnowledgeBase, ? extends TAnnotation> entry =
          entrySetIterator.next();

      final KnowledgeBase knowledgeBase = entry.getKey();
      final TAnnotation tableAnnotation = entry.getValue();

      mergeHeaders(mergedHeaderAnnotations, knowledgeBase, tableAnnotation);
      mergeCells(mergedCellAnnotations, knowledgeBase, tableAnnotation);
      mergeColumnRelations(mergedColumnRelations, knowledgeBase, tableAnnotation);
      mergeStatisticalAnnotations(mergedStatisticalAnnotations, knowledgeBase, tableAnnotation);
    }
  }

  private static ColumnPosition extractSubjectColumnPosition(final TAnnotation tableAnnotation) {
    final ColumnPosition subjectColumn = new ColumnPosition(tableAnnotation.getSubjectColumn());
    return subjectColumn;
  }

  private void mergeColumnRelations(
      final Map<ColumnRelationPosition, ColumnRelationAnnotation> mergedColumnRelations,
      final KnowledgeBase knowledgeBase, final TAnnotation tableAnnotation) {
    final Map<ColumnRelationPosition, ColumnRelationAnnotation> columnRelations =
        convertColumnRelations(knowledgeBase, tableAnnotation);
    Maps.mergeWith(mergedColumnRelations, columnRelations, (first, second) -> first.merge(second));
  }

  private void mergeCells(final CellAnnotation[][] mergedCellAnnotations,
      final KnowledgeBase knowledgeBase, final TAnnotation tableAnnotation) {
    final CellAnnotation[][] cellAnnotations =
        convertCellAnnotations(knowledgeBase, tableAnnotation);
    Arrays.zipMatrixWith(mergedCellAnnotations, cellAnnotations,
        (first, second) -> first.merge(second));
  }

  private void mergeHeaders(final List<HeaderAnnotation> mergedHeaderAnnotations,
      final KnowledgeBase knowledgeBase, final TAnnotation tableAnnotation) {
    final List<HeaderAnnotation> headerAnnotations =
        convertColumnAnnotations(knowledgeBase, tableAnnotation);
    Lists.zipWith(mergedHeaderAnnotations, headerAnnotations,
        (first, second) -> first.merge(second));
  }

  private void mergeStatisticalAnnotations(final List<StatisticalAnnotation> mergedStatisticalAnnotations,
      final KnowledgeBase knowledgeBase, final TAnnotation tableAnnotation) {
    final List<StatisticalAnnotation> statisticalAnnotations =
        convertStatisticalAnnotations(knowledgeBase, tableAnnotation);
    Lists.zipWith(mergedStatisticalAnnotations, statisticalAnnotations,
        (first, second) -> first.merge(second));
  }

  private Map<ColumnRelationPosition, ColumnRelationAnnotation> convertColumnRelations(
      KnowledgeBase knowledgeBase, TAnnotation original) {
    Map<ColumnRelationPosition, ColumnRelationAnnotation> columnRelations = new HashMap<>();
    for (Map.Entry<RelationColumns, List<TColumnColumnRelationAnnotation>> annotations : original
        .getColumncolumnRelations().entrySet()) {
      HashMap<KnowledgeBase, Set<EntityCandidate>> candidates = new HashMap<>();
      HashMap<KnowledgeBase, Set<EntityCandidate>> chosen = new HashMap<>();

      Set<EntityCandidate> candidatesSet = new HashSet<>();
      Set<EntityCandidate> chosenSet = new HashSet<>();

      candidates.put(knowledgeBase, candidatesSet);
      chosen.put(knowledgeBase, chosenSet);

      EntityCandidate bestCandidate = null;
      for (TColumnColumnRelationAnnotation annotation : annotations.getValue()) {
        Entity entity =
            entitiesFactory.create(annotation.getRelationURI(), annotation.getRelationLabel());
        Score likelihood = new Score(annotation.getFinalScore());

        EntityCandidate candidate = new EntityCandidate(entity, likelihood);

        candidatesSet.add(candidate);

        if (bestCandidate == null
            || bestCandidate.getScore().getValue() < candidate.getScore().getValue()) {
          bestCandidate = candidate;
        }
      }

      if (bestCandidate != null) {
        chosenSet.add(bestCandidate);
      }

      RelationColumns relationColumns = annotations.getKey();
      ColumnRelationPosition position =
          new ColumnRelationPosition(new ColumnPosition(relationColumns.getSubjectCol()),
              new ColumnPosition(relationColumns.getObjectCol()));
      ColumnRelationAnnotation relationAnnotation =
          new ColumnRelationAnnotation(candidates, chosen);
      columnRelations.put(position, relationAnnotation);
    }
    return columnRelations;
  }

  private CellAnnotation[][] convertCellAnnotations(KnowledgeBase knowledgeBase,
      TAnnotation original) {
    int columnCount = original.getCols();
    int rowCount = original.getRows();
    CellAnnotation[][] cellAnnotations = new CellAnnotation[rowCount][columnCount];

    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
      for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
        TCellAnnotation[] annotations = original.getContentCellAnnotations(rowIndex, columnIndex);

        HashMap<KnowledgeBase, Set<EntityCandidate>> candidates = new HashMap<>();
        HashMap<KnowledgeBase, Set<EntityCandidate>> chosen = new HashMap<>();

        if (annotations != null) {
          Set<EntityCandidate> candidatesSet = new HashSet<>();
          Set<EntityCandidate> chosenSet = new HashSet<>();

          candidates.put(knowledgeBase, candidatesSet);
          chosen.put(knowledgeBase, chosenSet);

          EntityCandidate bestCandidate = null;
          for (TCellAnnotation annotation : annotations) {
            uk.ac.shef.dcs.kbproxy.model.Entity clazz = annotation.getAnnotation();

            Entity entity = entitiesFactory.create(clazz.getId(), clazz.getLabel());
            Score likelihood = new Score(annotation.getFinalScore());

            EntityCandidate candidate = new EntityCandidate(entity, likelihood);

            candidatesSet.add(candidate);

            if (bestCandidate == null
                || bestCandidate.getScore().getValue() < candidate.getScore().getValue()) {
              bestCandidate = candidate;
            }
          }

          if (bestCandidate != null) {
            chosenSet.add(bestCandidate);
          }
        }

        CellAnnotation cellAnnotation = new CellAnnotation(candidates, chosen);
        cellAnnotations[rowIndex][columnIndex] = cellAnnotation;
      }
    }

    return cellAnnotations;
  }

  private List<HeaderAnnotation> convertColumnAnnotations(KnowledgeBase knowledgeBase,
      TAnnotation original) {
    int columnCount = original.getCols();
    List<HeaderAnnotation> headerAnnotations = new ArrayList<>(columnCount);

    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
      TColumnHeaderAnnotation[] annotations = original.getHeaderAnnotation(columnIndex);

      HashMap<KnowledgeBase, Set<EntityCandidate>> candidates = new HashMap<>();
      HashMap<KnowledgeBase, Set<EntityCandidate>> chosen = new HashMap<>();

      if (annotations != null) {
        Set<EntityCandidate> candidatesSet = new HashSet<>();
        Set<EntityCandidate> chosenSet = new HashSet<>();

        candidates.put(knowledgeBase, candidatesSet);
        chosen.put(knowledgeBase, chosenSet);

        EntityCandidate bestCandidate = null;
        for (TColumnHeaderAnnotation annotation : annotations) {
          Clazz clazz = annotation.getAnnotation();

          Entity entity = entitiesFactory.create(clazz.getId(), clazz.getLabel());
          Score likelihood = new Score(annotation.getFinalScore());

          EntityCandidate candidate = new EntityCandidate(entity, likelihood);

          candidatesSet.add(candidate);

          if (bestCandidate == null
              || bestCandidate.getScore().getValue() < candidate.getScore().getValue()) {
            bestCandidate = candidate;
          }
        }

        if (bestCandidate != null) {
          chosenSet.add(bestCandidate);
        }
      }

      HeaderAnnotation headerAnnotation = new HeaderAnnotation(candidates, chosen);
      headerAnnotations.add(headerAnnotation);
    }

    return headerAnnotations;
  }

  private List<StatisticalAnnotation> convertStatisticalAnnotations(KnowledgeBase knowledgeBase,
      TAnnotation original) {
    int columnCount = original.getCols();
    List<StatisticalAnnotation> statisticalAnnotations = new ArrayList<>(columnCount);

    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
      TStatisticalAnnotation annotation = original.getStatisticalAnnotation(columnIndex);

      HashMap<KnowledgeBase, ComponentTypeValue> component = new HashMap<>();
      HashMap<KnowledgeBase, Set<EntityCandidate>> predicate = new HashMap<>();

      if (annotation != null) {
        ComponentTypeValue componentType;
        Set<EntityCandidate> predicateSet = new HashSet<>();

        switch (annotation.getComponent()) {
          case DIMENSION:
            componentType = ComponentTypeValue.DIMENSION;
            break;
          case MEASURE:
            componentType = ComponentTypeValue.MEASURE;
            break;
          case NONE:
            componentType = ComponentTypeValue.NONE;
            break;
          default:
            componentType = ComponentTypeValue.NONE;
            break;
        }

        component.put(knowledgeBase, componentType);
        predicate.put(knowledgeBase, predicateSet);

        if (annotation.getPredicateURI() != null && annotation.getPredicateLabel() != null) {
          Entity entity = entitiesFactory.create(annotation.getPredicateURI(), annotation.getPredicateLabel());
          Score likelihood = new Score(annotation.getScore());

          EntityCandidate candidate = new EntityCandidate(entity, likelihood);

          predicateSet.add(candidate);
        }
      }

      StatisticalAnnotation statisticalAnnotation = new StatisticalAnnotation(component, predicate);
      statisticalAnnotations.add(statisticalAnnotation);
    }

    return statisticalAnnotations;
  }
}
