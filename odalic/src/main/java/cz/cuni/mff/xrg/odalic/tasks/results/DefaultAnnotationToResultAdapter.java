package cz.cuni.mff.xrg.odalic.tasks.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.concurrent.Immutable;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnProcessingTypeValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.ComponentTypeValue;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.entities.EntitiesFactory;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnProcessingAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
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
import uk.ac.shef.dcs.sti.core.model.TColumnProcessingAnnotation;
import uk.ac.shef.dcs.sti.core.model.TStatisticalAnnotation;

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

  private static ColumnPosition extractSubjectColumnPosition(final TAnnotation tableAnnotation) {
    return new ColumnPosition(tableAnnotation.getSubjectColumn());
  }

  private static Map<String, ColumnPosition> extractSubjectColumnPositions(
      final Map<? extends KnowledgeBase, ? extends TAnnotation> basesToTableAnnotations) {
    final ImmutableMap.Builder<String, ColumnPosition> subjectColumnPositionsBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends TAnnotation> annotationEntry : basesToTableAnnotations
        .entrySet()) {
      final KnowledgeBase base = annotationEntry.getKey();
      final ColumnPosition subjectColumnPosition =
          extractSubjectColumnPosition(annotationEntry.getValue());

      subjectColumnPositionsBuilder.put(base.getName(), subjectColumnPosition);
    }
    return subjectColumnPositionsBuilder.build();
  }

  private static Set<ColumnPosition> extractOtherSubjectColumnPositionSet(final TAnnotation tableAnnotation) {
    Set<ColumnPosition> otherSubjectPositions = new HashSet<>();
    for (Integer columnIndex : tableAnnotation.getOtherSubjectColumns()) {
      otherSubjectPositions.add(new ColumnPosition(columnIndex));
    }
    return otherSubjectPositions;
  }

  private static Map<String, Set<ColumnPosition>> extractOtherSubjectColumnPositions(
      final Map<? extends KnowledgeBase, ? extends TAnnotation> basesToTableAnnotations) {
    final ImmutableMap.Builder<String, Set<ColumnPosition>> otherSubjectColumnPositionsBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends TAnnotation> annotationEntry : basesToTableAnnotations
        .entrySet()) {
      final String base = annotationEntry.getKey().getName();
      final Set<ColumnPosition> otherSubjectColumnPositions =
          extractOtherSubjectColumnPositionSet(annotationEntry.getValue());

      otherSubjectColumnPositionsBuilder.put(base, otherSubjectColumnPositions);
    }
    return otherSubjectColumnPositionsBuilder.build();
  }

  
  private static String getCellWarning(final String knowledgeBaseName, final int row,
      final int column, final String warning) {
    return String.format("%1$s - Cell on row %2$d, column %3$d - %4$s", knowledgeBaseName,
        row + 1, column + 1, warning);
  }

  private static String getHeaderWarning(final String knowledgeBaseName, final int column,
      final String warning) {
    return String.format("%1$s - Header %2$d - %3$s", knowledgeBaseName, column + 1, warning);
  }

  private static String getRelationWarning(final String knowledgeBaseName,
      final RelationColumns relationColumns, final String warning) {
    return String.format("%1$s - Relation %2$d -> %3$d - %4$s", knowledgeBaseName,
        relationColumns.getSubjectCol() + 1, relationColumns.getObjectCol() + 1, warning);
  }

  private static List<String> getWarnings(final String knowledgeBaseName,
      final TAnnotation original) {
    final int columnCount = original.getCols();
    final int rowCount = original.getRows();

    final List<String> result = original.getColumnRelationWarnings().entrySet().stream()
        .flatMap(entry -> entry.getValue().stream()
            .map(warning -> getRelationWarning(knowledgeBaseName, entry.getKey(), warning)))
        .collect(Collectors.toList());

    for (int column = 0; column < columnCount; column++) {
      final int columnFinal = column;

      for (int row = 0; row < rowCount; row++) {
        final int rowFinal = row;

        result.addAll(original.getContentWarnings(row, column).stream()
            .map(warning -> getCellWarning(knowledgeBaseName, rowFinal, columnFinal, warning))
            .collect(Collectors.toList()));
      }
      result.addAll(original.getHeaderWarnings(column).stream()
          .map(warning -> getHeaderWarning(knowledgeBaseName, columnFinal, warning))
          .collect(Collectors.toList()));
    }

    return result;
  }

  private static Iterator<? extends Map.Entry<? extends KnowledgeBase, ? extends TAnnotation>> initializeEntrySetIterator(
      final Map<? extends KnowledgeBase, ? extends TAnnotation> basesToTableAnnotations) {
    final Set<? extends Map.Entry<? extends KnowledgeBase, ? extends TAnnotation>> entrySet =
        basesToTableAnnotations.entrySet();
    return entrySet.iterator();
  }

  private final EntitiesFactory entitiesFactory;

  @Autowired
  public DefaultAnnotationToResultAdapter(final EntitiesFactory entitesFactory) {
    Preconditions.checkNotNull(entitesFactory);

    this.entitiesFactory = entitesFactory;
  }

  private CellAnnotation[][] convertCellAnnotations(final String knowledgeBase,
      final TAnnotation original) {
    final int columnCount = original.getCols();
    final int rowCount = original.getRows();
    final CellAnnotation[][] cellAnnotations = new CellAnnotation[rowCount][columnCount];

    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
      for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
        final TCellAnnotation[] annotations =
            original.getContentCellAnnotations(rowIndex, columnIndex);

        final HashMap<String, Set<EntityCandidate>> candidates = new HashMap<>();
        final HashMap<String, Set<EntityCandidate>> chosen = new HashMap<>();

        if (annotations != null) {
          final Set<EntityCandidate> candidatesSet = new HashSet<>();
          final Set<EntityCandidate> chosenSet = new HashSet<>();

          candidates.put(knowledgeBase, candidatesSet);
          chosen.put(knowledgeBase, chosenSet);

          EntityCandidate bestCandidate = null;
          for (final TCellAnnotation annotation : annotations) {
            final uk.ac.shef.dcs.kbproxy.model.Entity clazz = annotation.getAnnotation();

            final Entity entity = this.entitiesFactory.create(clazz.getId(), clazz.getLabel());
            final Score likelihood = new Score(annotation.getFinalScore());

            final EntityCandidate candidate = new EntityCandidate(entity, likelihood);

            candidatesSet.add(candidate);

            if ((bestCandidate == null)
                || (bestCandidate.getScore().getValue() < candidate.getScore().getValue())) {
              bestCandidate = candidate;
            }
          }

          if (bestCandidate != null) {
            chosenSet.add(bestCandidate);
          }
        }

        final CellAnnotation cellAnnotation = new CellAnnotation(candidates, chosen);
        cellAnnotations[rowIndex][columnIndex] = cellAnnotation;
      }
    }

    return cellAnnotations;
  }

  private List<HeaderAnnotation> convertColumnAnnotations(final String knowledgeBaseName,
      final TAnnotation original) {
    final int columnCount = original.getCols();
    final List<HeaderAnnotation> headerAnnotations = new ArrayList<>(columnCount);

    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
      final TColumnHeaderAnnotation[] annotations = original.getHeaderAnnotation(columnIndex);

      final HashMap<String, Set<EntityCandidate>> candidates = new HashMap<>();
      final HashMap<String, Set<EntityCandidate>> chosen = new HashMap<>();

      if (annotations != null) {
        final Set<EntityCandidate> candidatesSet = new HashSet<>();
        final Set<EntityCandidate> chosenSet = new HashSet<>();

        candidates.put(knowledgeBaseName, candidatesSet);
        chosen.put(knowledgeBaseName, chosenSet);

        EntityCandidate bestCandidate = null;
        for (final TColumnHeaderAnnotation annotation : annotations) {
          final Clazz clazz = annotation.getAnnotation();

          final Entity entity = this.entitiesFactory.create(clazz.getId(), clazz.getLabel());
          final Score likelihood = new Score(annotation.getFinalScore());

          final EntityCandidate candidate = new EntityCandidate(entity, likelihood);

          candidatesSet.add(candidate);

          if ((bestCandidate == null)
              || (bestCandidate.getScore().getValue() < candidate.getScore().getValue())) {
            bestCandidate = candidate;
          }
        }

        if (bestCandidate != null) {
          chosenSet.add(bestCandidate);
        }
      }

      final HeaderAnnotation headerAnnotation = new HeaderAnnotation(candidates, chosen);
      headerAnnotations.add(headerAnnotation);
    }

    return headerAnnotations;
  }

  private List<ColumnProcessingAnnotation> convertColumnProcessingAnnotations(
      final String knowledgeBaseName, final TAnnotation original) {
    final int columnCount = original.getCols();
    final List<ColumnProcessingAnnotation> columnProcessingAnnotations =
        new ArrayList<>(columnCount);

    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
      final TColumnProcessingAnnotation annotation =
          original.getColumnProcessingAnnotation(columnIndex);

      final HashMap<String, ColumnProcessingTypeValue> processingType = new HashMap<>();

      if (annotation != null) {
        ColumnProcessingTypeValue processingTypeValue;

        switch (annotation.getProcessingType()) {
          case NAMED_ENTITY:
            processingTypeValue = ColumnProcessingTypeValue.NAMED_ENTITY;
            break;
          case NON_NAMED_ENTITY:
            processingTypeValue = ColumnProcessingTypeValue.NON_NAMED_ENTITY;
            break;
          case IGNORED:
            processingTypeValue = ColumnProcessingTypeValue.IGNORED;
            break;
          case COMPULSORY:
            processingTypeValue = ColumnProcessingTypeValue.COMPULSORY;
            break;
          default:
            processingTypeValue = ColumnProcessingTypeValue.NAMED_ENTITY;
            break;
        }

        processingType.put(knowledgeBaseName, processingTypeValue);
      }

      final ColumnProcessingAnnotation columnProcessingAnnotation =
          new ColumnProcessingAnnotation(processingType);
      columnProcessingAnnotations.add(columnProcessingAnnotation);
    }

    return columnProcessingAnnotations;
  }

  private Map<ColumnRelationPosition, ColumnRelationAnnotation> convertColumnRelations(
      final String knowledgeBaseName, final TAnnotation original) {
    final Map<ColumnRelationPosition, ColumnRelationAnnotation> columnRelations = new HashMap<>();
    for (final Map.Entry<RelationColumns, List<TColumnColumnRelationAnnotation>> annotations : original
        .getColumncolumnRelations().entrySet()) {
      final HashMap<String, Set<EntityCandidate>> candidates = new HashMap<>();
      final HashMap<String, Set<EntityCandidate>> chosen = new HashMap<>();

      final Set<EntityCandidate> candidatesSet = new HashSet<>();
      final Set<EntityCandidate> chosenSet = new HashSet<>();

      candidates.put(knowledgeBaseName, candidatesSet);
      chosen.put(knowledgeBaseName, chosenSet);

      EntityCandidate bestCandidate = null;
      for (final TColumnColumnRelationAnnotation annotation : annotations.getValue()) {
        final Entity entity =
            this.entitiesFactory.create(annotation.getRelationURI(), annotation.getRelationLabel());
        final Score likelihood = new Score(annotation.getFinalScore());

        final EntityCandidate candidate = new EntityCandidate(entity, likelihood);

        candidatesSet.add(candidate);

        if ((bestCandidate == null)
            || (bestCandidate.getScore().getValue() < candidate.getScore().getValue())) {
          bestCandidate = candidate;
        }
      }

      if (bestCandidate != null) {
        chosenSet.add(bestCandidate);
      }

      final RelationColumns relationColumns = annotations.getKey();
      final ColumnRelationPosition position =
          new ColumnRelationPosition(new ColumnPosition(relationColumns.getSubjectCol()),
              new ColumnPosition(relationColumns.getObjectCol()));
      final ColumnRelationAnnotation relationAnnotation =
          new ColumnRelationAnnotation(candidates, chosen);
      columnRelations.put(position, relationAnnotation);
    }
    return columnRelations;
  }

  private List<StatisticalAnnotation> convertStatisticalAnnotations(
      final String knowledgeBaseName, final TAnnotation original) {
    final int columnCount = original.getCols();
    final List<StatisticalAnnotation> statisticalAnnotations = new ArrayList<>(columnCount);

    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
      final TStatisticalAnnotation annotation = original.getStatisticalAnnotation(columnIndex);

      final HashMap<String, ComponentTypeValue> component = new HashMap<>();
      final HashMap<String, Set<EntityCandidate>> predicate = new HashMap<>();

      if (annotation != null) {
        ComponentTypeValue componentType;
        final Set<EntityCandidate> predicateSet = new HashSet<>();

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

        component.put(knowledgeBaseName, componentType);
        predicate.put(knowledgeBaseName, predicateSet);

        if ((annotation.getPredicateURI() != null) && (annotation.getPredicateLabel() != null)) {
          final Entity entity = this.entitiesFactory.create(annotation.getPredicateURI(),
              annotation.getPredicateLabel());
          final Score likelihood = new Score(annotation.getScore());

          final EntityCandidate candidate = new EntityCandidate(entity, likelihood);

          predicateSet.add(candidate);
        }
      }

      final StatisticalAnnotation statisticalAnnotation =
          new StatisticalAnnotation(component, predicate);
      statisticalAnnotations.add(statisticalAnnotation);
    }

    return statisticalAnnotations;
  }

  private void mergeCells(final CellAnnotation[][] mergedCellAnnotations,
      final String knowledgeBaseName, final TAnnotation tableAnnotation) {
    final CellAnnotation[][] cellAnnotations =
        convertCellAnnotations(knowledgeBaseName, tableAnnotation);
    Arrays.zipMatrixWith(mergedCellAnnotations, cellAnnotations, CellAnnotation::merge);
  }

  private void mergeColumnProcessingAnnotations(
      final List<ColumnProcessingAnnotation> mergedColumnProcessingAnnotations,
      final String knowledgeBaseName, final TAnnotation tableAnnotation) {
    final List<ColumnProcessingAnnotation> columnProcessingAnnotations =
        convertColumnProcessingAnnotations(knowledgeBaseName, tableAnnotation);
    Lists.zipWith(mergedColumnProcessingAnnotations, columnProcessingAnnotations,
        ColumnProcessingAnnotation::merge);
  }

  private void mergeColumnRelations(
      final Map<ColumnRelationPosition, ColumnRelationAnnotation> mergedColumnRelations,
      final String knowledgeBaseName, final TAnnotation tableAnnotation) {
    final Map<ColumnRelationPosition, ColumnRelationAnnotation> columnRelations =
        convertColumnRelations(knowledgeBaseName, tableAnnotation);
    Maps.mergeWith(mergedColumnRelations, columnRelations, ColumnRelationAnnotation::merge);
  }

  private void mergeHeaders(final List<HeaderAnnotation> mergedHeaderAnnotations,
      final String knowledgeBaseName, final TAnnotation tableAnnotation) {
    final List<HeaderAnnotation> headerAnnotations =
        convertColumnAnnotations(knowledgeBaseName, tableAnnotation);
    Lists.zipWith(mergedHeaderAnnotations, headerAnnotations, HeaderAnnotation::merge);
  }

  private void mergeStatisticalAnnotations(
      final List<StatisticalAnnotation> mergedStatisticalAnnotations,
      final String knowledgeBaseName, final TAnnotation tableAnnotation) {
    final List<StatisticalAnnotation> statisticalAnnotations =
        convertStatisticalAnnotations(knowledgeBaseName, tableAnnotation);
    Lists.zipWith(mergedStatisticalAnnotations, statisticalAnnotations,
        StatisticalAnnotation::merge);
  }

  private void mergeWarnings(final List<String> mergedWarnings, final String knowledgeBaseName,
      final TAnnotation tableAnnotation) {
    final List<String> warnings = getWarnings(knowledgeBaseName, tableAnnotation);
    mergedWarnings.addAll(warnings);
  }

  private void processTheRest(
      final Iterator<? extends Map.Entry<? extends KnowledgeBase, ? extends TAnnotation>> entrySetIterator,
      final List<HeaderAnnotation> mergedHeaderAnnotations,
      final CellAnnotation[][] mergedCellAnnotations,
      final Map<ColumnRelationPosition, ColumnRelationAnnotation> mergedColumnRelations,
      final List<StatisticalAnnotation> mergedStatisticalAnnotations,
      final List<ColumnProcessingAnnotation> mergedColumnProcessingAnnotations,
      final List<String> mergedWarnings) {
    while (entrySetIterator.hasNext()) {
      final Map.Entry<? extends KnowledgeBase, ? extends TAnnotation> entry =
          entrySetIterator.next();

      final KnowledgeBase knowledgeBase = entry.getKey();
      final TAnnotation tableAnnotation = entry.getValue();

      final String knowledgeBaseName = knowledgeBase.getName();
      
      mergeHeaders(mergedHeaderAnnotations, knowledgeBaseName, tableAnnotation);
      mergeCells(mergedCellAnnotations, knowledgeBaseName, tableAnnotation);
      mergeColumnRelations(mergedColumnRelations, knowledgeBaseName, tableAnnotation);
      mergeStatisticalAnnotations(mergedStatisticalAnnotations, knowledgeBaseName, tableAnnotation);
      mergeColumnProcessingAnnotations(mergedColumnProcessingAnnotations, knowledgeBaseName,
          tableAnnotation);
      mergeWarnings(mergedWarnings, knowledgeBaseName, tableAnnotation);
    }
  }

  /**
   * This implementation demands that the subject columns recognized in the annotations are the
   * same.
   *
   * @see cz.cuni.mff.xrg.odalic.tasks.results.AnnotationToResultAdapter#toResult(Map)
   */
  @Override
  public Result toResult(
      final Map<? extends KnowledgeBase, ? extends TAnnotation> basesToTableAnnotations) {
    Preconditions.checkArgument(!basesToTableAnnotations.isEmpty());

    // Extract subject column positions.
    final Map<String, ColumnPosition> subjectColumnPositions =
        extractSubjectColumnPositions(basesToTableAnnotations);

    // Extract other subject column positions.
    final Map<String, Set<ColumnPosition>> otherSubjectColumnPositions =
        extractOtherSubjectColumnPositions(basesToTableAnnotations);

    // Merge annotations.
    final Iterator<? extends Map.Entry<? extends KnowledgeBase, ? extends TAnnotation>> entrySetIterator =
        initializeEntrySetIterator(basesToTableAnnotations);

    // Process the first entry to initialize working structures for the merges.
    final Map.Entry<? extends KnowledgeBase, ? extends TAnnotation> firstEntry =
        entrySetIterator.next();
    final KnowledgeBase firstKnowledgeBase = firstEntry.getKey();
    final TAnnotation firstTableAnnotation = firstEntry.getValue();

    final String firstKnowledgeBaseName = firstKnowledgeBase.getName(); 
    
    final List<HeaderAnnotation> mergedHeaderAnnotations =
        convertColumnAnnotations(firstKnowledgeBaseName, firstTableAnnotation);
    final CellAnnotation[][] mergedCellAnnotations =
        convertCellAnnotations(firstKnowledgeBaseName, firstTableAnnotation);
    final Map<ColumnRelationPosition, ColumnRelationAnnotation> mergedColumnRelations =
        convertColumnRelations(firstKnowledgeBaseName, firstTableAnnotation);
    final List<StatisticalAnnotation> mergedStatisticalAnnotations =
        convertStatisticalAnnotations(firstKnowledgeBaseName, firstTableAnnotation);
    final List<ColumnProcessingAnnotation> mergedColumnProcessingAnnotations =
        convertColumnProcessingAnnotations(firstKnowledgeBaseName, firstTableAnnotation);
    final List<String> mergedWarnings = getWarnings(firstKnowledgeBaseName, firstTableAnnotation);

    // Process the rest.
    processTheRest(entrySetIterator, mergedHeaderAnnotations, mergedCellAnnotations,
        mergedColumnRelations, mergedStatisticalAnnotations, mergedColumnProcessingAnnotations,
        mergedWarnings);

    Collections.sort(mergedWarnings);

    return new Result(subjectColumnPositions, otherSubjectColumnPositions,
        mergedHeaderAnnotations, mergedCellAnnotations, mergedColumnRelations,
        mergedStatisticalAnnotations, mergedColumnProcessingAnnotations,
        mergedWarnings);
  }
}
