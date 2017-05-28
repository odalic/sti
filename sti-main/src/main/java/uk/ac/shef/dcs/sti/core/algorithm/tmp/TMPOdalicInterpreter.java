package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.extension.annotations.ComponentTypeValue;
import uk.ac.shef.dcs.sti.core.extension.annotations.EntityCandidate;
import uk.ac.shef.dcs.sti.core.extension.constraints.Ambiguity;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.extension.constraints.DataCubeComponent;
import uk.ac.shef.dcs.sti.core.extension.positions.CellPosition;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnProcessingAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnProcessingAnnotation.TColumnProcessingType;
import uk.ac.shef.dcs.sti.core.model.TStatisticalAnnotation;
import uk.ac.shef.dcs.sti.core.model.TStatisticalAnnotation.TComponentType;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.util.Pair;

public class TMPOdalicInterpreter extends SemanticTableInterpreter {

  private static final Logger LOG = LoggerFactory.getLogger(TMPOdalicInterpreter.class.getName());
  private final SubjectColumnDetector subjectColumnDetector;
  private final LEARNING learning;
  private final LiteralColumnTagger literalColumnTagger;
  private final TColumnColumnRelationEnumerator relationEnumerator;

  private final UPDATE update;

  public TMPOdalicInterpreter(final SubjectColumnDetector subjectColumnDetector,
      final LEARNING learning, final UPDATE update,
      final TColumnColumnRelationEnumerator relationEnumerator,
      final LiteralColumnTagger literalColumnTagger) {
    super(new int[0], new int[0]);
    this.subjectColumnDetector = subjectColumnDetector;
    this.learning = learning;
    this.literalColumnTagger = literalColumnTagger;
    this.relationEnumerator = relationEnumerator;
    this.update = update;
  }

  private TComponentType convert(final ComponentTypeValue componentType) {
    switch (componentType) {
      case DIMENSION:
        return TComponentType.DIMENSION;
      case MEASURE:
        return TComponentType.MEASURE;
      case NONE:
        return TComponentType.NONE;
      default:
        return TComponentType.NONE;
    }
  }

  private void setStatisticalAnnotations(final List<Integer> annotatedColumns, final Table table,
      final TAnnotation tableAnnotations, final Constraints constraints) {
    // set data cube components suggested by user
    for (final DataCubeComponent dataCubeComponent : constraints.getDataCubeComponents()) {
      if (dataCubeComponent.getAnnotation().getPredicate().isEmpty()) {
        tableAnnotations.setStatisticalAnnotation(dataCubeComponent.getPosition().getIndex(),
            new TStatisticalAnnotation(TComponentType.NONE, null, null, 0));
      } else {
        final EntityCandidate suggestion =
            dataCubeComponent.getAnnotation().getPredicate().iterator().next();
        tableAnnotations.setStatisticalAnnotation(dataCubeComponent.getPosition().getIndex(),
            new TStatisticalAnnotation(convert(dataCubeComponent.getAnnotation().getComponent()),
                suggestion.getEntity().getResource(), suggestion.getEntity().getLabel(),
                suggestion.getScore().getValue()));
      }
    }

    // set data cube components for other columns (without user suggestions)
    for (int col = 0; col < table.getNumCols(); col++) {
      if (tableAnnotations.getStatisticalAnnotation(col) == null) {
        if (!annotatedColumns.contains(col)) {
          tableAnnotations.setStatisticalAnnotation(col,
              new TStatisticalAnnotation(TComponentType.NONE, null, null, 0));
        } else {
          if (table.getColumnHeader(col).getFeature().getMostFrequentDataType().getType()
              .equals(DataTypeClassifier.DataType.NAMED_ENTITY)) {
            tableAnnotations.setStatisticalAnnotation(col,
                new TStatisticalAnnotation(TComponentType.DIMENSION, null, null, 0));
          } else {
            tableAnnotations.setStatisticalAnnotation(col,
                new TStatisticalAnnotation(TComponentType.MEASURE, null, null, 0));
          }
        }
      }
    }
  }

  private Set<Ambiguity> setColumnProcessingAnnotationsAndAmbiguities(final Table table,
      final TAnnotation tableAnnotations, final Constraints constraints) {
    final Set<Ambiguity> ambiguities = new HashSet<>(constraints.getAmbiguities());

    for (int col = 0; col < table.getNumCols(); col++) {
      if (getIgnoreColumns().contains(col)) {
        // when the column is ignored, set processing type to IGNORED
        tableAnnotations.setColumnProcessingAnnotation(col,
            new TColumnProcessingAnnotation(TColumnProcessingType.IGNORED));
      } else if (isCompulsoryColumn(col)) {
        // when the column is compulsory, set processing type to COMPULSORY
        tableAnnotations.setColumnProcessingAnnotation(col,
            new TColumnProcessingAnnotation(TColumnProcessingType.COMPULSORY));
      } else if (table.getColumnHeader(col).getFeature().getMostFrequentDataType().getType()
          .equals(DataTypeClassifier.DataType.NAMED_ENTITY)) {
        // when the column's most frequent data type is Named entity,
        // set processing type to NAMED_ENTITY
        tableAnnotations.setColumnProcessingAnnotation(col,
            new TColumnProcessingAnnotation(TColumnProcessingType.NAMED_ENTITY));
      } else {
        // otherwise (i.e. the column does not contain Named entity as the most frequent data type),
        // set processing type to NON_NAMED_ENTITY
        tableAnnotations.setColumnProcessingAnnotation(col,
            new TColumnProcessingAnnotation(TColumnProcessingType.NON_NAMED_ENTITY));
        // and in this case we will not disambiguate (and so classify) them,
        // except for the user defined constraints
        for (int row = 0; row < table.getNumRows(); row++) {
          if (!constraints.existDisambChosenForCell(col, row)) {
            ambiguities.add(new Ambiguity(new CellPosition(row, col)));
          }
        }
      }
    }

    return ambiguities;
  }

  @Override
  public TAnnotation start(final Table table, final boolean relationLearning) throws STIException {
    return start(table, !relationLearning, new Constraints());
  }

  @Override
  public TAnnotation start(final Table table, final boolean statistical, Constraints constraints)
      throws STIException {
    Preconditions.checkNotNull(constraints, "The constraints cannot be null!");

    // set ignored columns
    final Set<Integer> ignoreCols = constraints.getColumnIgnores().stream()
        .map(e -> e.getPosition().getIndex()).collect(Collectors.toSet());

    setIgnoreColumns(ignoreCols);

    final int[] ignoreColumnsArray =
        getIgnoreColumns().stream().mapToInt(e -> e.intValue()).sorted().toArray();
    this.literalColumnTagger.setIgnoreColumns(ignoreColumnsArray);

    // set compulsory columns
    final Set<Integer> mustdoCols = constraints.getColumnCompulsory().stream()
        .map(e -> e.getPosition().getIndex()).collect(Collectors.toSet());

    setMustdoColumns(mustdoCols);

    try {
      final TAnnotation tableAnnotations = new TAnnotation(table.getNumRows(), table.getNumCols());

      // find the main subject column of this table
      LOG.info("\t> PHASE: Detecting subject column ...");
      final List<Pair<Integer, Pair<Double, Boolean>>> subjectColumnScores =
          this.subjectColumnDetector.compute(table, ignoreColumnsArray);
      tableAnnotations.setSubjectColumn(subjectColumnScores.get(0).getKey());

      // set column processing annotations
      final Set<Ambiguity> newAmbiguities = setColumnProcessingAnnotationsAndAmbiguities(table,
          tableAnnotations, constraints);

      constraints = new Constraints(constraints.getSubjectColumnsPositions(),
          constraints.getColumnIgnores(), constraints.getColumnCompulsory(),
          constraints.getColumnAmbiguities(),
          constraints.getClassifications(), constraints.getColumnRelations(),
          constraints.getDisambiguations(), newAmbiguities, constraints.getDataCubeComponents());

      // learning phase
      final List<Integer> annotatedColumns = new ArrayList<>();
      LOG.info("\t> PHASE: LEARNING ...");
      for (int col = 0; col < table.getNumCols(); col++) {
        if (getIgnoreColumns().contains(col)) {
          continue;
        }
        annotatedColumns.add(col);

        LOG.info("\t>> Column=" + col);
        this.learning.learn(table, tableAnnotations, col, constraints);
      }

      // update phase
      if (this.update != null) {
        LOG.info("\t> PHASE: UPDATE phase ...");
        this.update.update(annotatedColumns, table, tableAnnotations, constraints);
      }

      // set statistical annotations or discover relations
      if (statistical) {
        LOG.info("\t> PHASE: Statistical annotation enumeration ...");
        setStatisticalAnnotations(annotatedColumns, table, tableAnnotations, constraints);
      } else {
        LOG.info("\t> PHASE: RELATION ENUMERATION ...");
        if (constraints.getSubjectColumnsPositions().isEmpty()) {
          new RELATIONENUMERATION().enumerate(subjectColumnScores, getIgnoreColumns(),
              this.relationEnumerator, tableAnnotations, table, annotatedColumns, this.update,
              constraints);
        } else {
          new RELATIONENUMERATION().enumerate(
              this.relationEnumerator, tableAnnotations, table, annotatedColumns, this.update,
              constraints);
        }

        // consolidation - for columns that have relation with subject columns:
        // if the column is entity column, do column typing and disambiguation;
        // otherwise, simply create header annotation
        LOG.info("\t\t>> Annotate literal-columns in relation with main column");
        this.literalColumnTagger.annotate(table, tableAnnotations, constraints,
            annotatedColumns.toArray(new Integer[0]));
      }

      return tableAnnotations;
    } catch (final Exception e) {
      throw new STIException(e);
    }
  }
}
