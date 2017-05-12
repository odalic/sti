/**
 *
 */
package cz.cuni.mff.xrg.odalic.feedbacks;

import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.values.ComponentTypeValue;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.positions.CellPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Score;
import cz.cuni.mff.xrg.odalic.tasks.annotations.StatisticalAnnotation;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;

/**
 * Default {@link FeedbackToConstraintsAdapter} implementation.
 *
 * @author Václav Brodec
 *
 */
public class DefaultFeedbackToConstraintsAdapter implements FeedbackToConstraintsAdapter {

  private static uk.ac.shef.dcs.sti.core.extension.constraints.Ambiguity convert(
      final Ambiguity e) {
    return new uk.ac.shef.dcs.sti.core.extension.constraints.Ambiguity(convert(e.getPosition()));
  }

  private static uk.ac.shef.dcs.sti.core.extension.annotations.CellAnnotation convert(
      final CellAnnotation e, final KnowledgeBase base) {
    final NavigableSet<EntityCandidate> candidates = e.getCandidates().get(base.getName());
    if (candidates == null) {
      return null;
    }

    final Set<EntityCandidate> chosen = e.getChosen().get(base.getName());
    if (chosen == null) {
      return null;
    }

    return new uk.ac.shef.dcs.sti.core.extension.annotations.CellAnnotation(
        convertCandidates(candidates), convertCandidates(chosen));
  }

  private static uk.ac.shef.dcs.sti.core.extension.positions.CellPosition convert(
      final CellPosition e) {
    return new uk.ac.shef.dcs.sti.core.extension.positions.CellPosition(e.getRowIndex(),
        e.getColumnIndex());
  }

  private static uk.ac.shef.dcs.sti.core.extension.constraints.Classification convert(
      final Classification e, final KnowledgeBase base) {
    final uk.ac.shef.dcs.sti.core.extension.annotations.HeaderAnnotation convertedAnnotation =
        convert(e.getAnnotation(), base);
    if (convertedAnnotation == null) {
      return null;
    }

    return new uk.ac.shef.dcs.sti.core.extension.constraints.Classification(
        convert(e.getPosition()), convertedAnnotation);
  }

  private static uk.ac.shef.dcs.sti.core.extension.constraints.ColumnAmbiguity convert(
      final ColumnAmbiguity e) {
    return new uk.ac.shef.dcs.sti.core.extension.constraints.ColumnAmbiguity(
        convert(e.getPosition()));
  }

  private static uk.ac.shef.dcs.sti.core.extension.constraints.ColumnIgnore convert(
      final ColumnIgnore e) {
    return new uk.ac.shef.dcs.sti.core.extension.constraints.ColumnIgnore(convert(e.getPosition()));
  }

  private static uk.ac.shef.dcs.sti.core.extension.constraints.ColumnCompulsory convert(
      final ColumnCompulsory e) {
    return new uk.ac.shef.dcs.sti.core.extension.constraints.ColumnCompulsory(convert(e.getPosition()));
  }

  private static uk.ac.shef.dcs.sti.core.extension.positions.ColumnPosition convert(
      final ColumnPosition columnPosition) {
    return new uk.ac.shef.dcs.sti.core.extension.positions.ColumnPosition(
        columnPosition.getIndex());
  }

  private static uk.ac.shef.dcs.sti.core.extension.constraints.ColumnRelation convert(
      final ColumnRelation e, final KnowledgeBase base) {
    final uk.ac.shef.dcs.sti.core.extension.annotations.ColumnRelationAnnotation convertedAnnotation =
        convert(e.getAnnotation(), base);
    if (convertedAnnotation == null) {
      return null;
    }

    return new uk.ac.shef.dcs.sti.core.extension.constraints.ColumnRelation(
        convert(e.getPosition()), convertedAnnotation);
  }

  private static uk.ac.shef.dcs.sti.core.extension.annotations.ColumnRelationAnnotation convert(
      final ColumnRelationAnnotation e, final KnowledgeBase base) {
    final NavigableSet<EntityCandidate> candidates = e.getCandidates().get(base.getName());
    if (candidates == null) {
      return null;
    }

    final Set<EntityCandidate> chosen = e.getChosen().get(base.getName());
    if (chosen == null) {
      return null;
    }

    return new uk.ac.shef.dcs.sti.core.extension.annotations.ColumnRelationAnnotation(
        convertCandidates(candidates), convertCandidates(chosen));
  }

  private static uk.ac.shef.dcs.sti.core.extension.positions.ColumnRelationPosition convert(
      final ColumnRelationPosition e) {
    return new uk.ac.shef.dcs.sti.core.extension.positions.ColumnRelationPosition(
        convert(e.getFirst()), convert(e.getSecond()));
  }

  private static uk.ac.shef.dcs.sti.core.extension.annotations.ComponentTypeValue convert(
      final ComponentTypeValue componentType) {
    switch (componentType) {
      case DIMENSION:
        return uk.ac.shef.dcs.sti.core.extension.annotations.ComponentTypeValue.DIMENSION;
      case MEASURE:
        return uk.ac.shef.dcs.sti.core.extension.annotations.ComponentTypeValue.MEASURE;
      case NONE:
        return uk.ac.shef.dcs.sti.core.extension.annotations.ComponentTypeValue.NONE;
      default:
        return uk.ac.shef.dcs.sti.core.extension.annotations.ComponentTypeValue.NONE;
    }
  }

  private static uk.ac.shef.dcs.sti.core.extension.constraints.DataCubeComponent convert(
      final DataCubeComponent e, final KnowledgeBase base) {
    final uk.ac.shef.dcs.sti.core.extension.annotations.StatisticalAnnotation convertedAnnotation =
        convert(e.getAnnotation(), base);
    if (convertedAnnotation == null) {
      return null;
    }

    return new uk.ac.shef.dcs.sti.core.extension.constraints.DataCubeComponent(
        convert(e.getPosition()), convertedAnnotation);
  }

  private static uk.ac.shef.dcs.sti.core.extension.constraints.Disambiguation convert(
      final Disambiguation e, final KnowledgeBase base) {
    final uk.ac.shef.dcs.sti.core.extension.annotations.CellAnnotation convertedAnnotation =
        convert(e.getAnnotation(), base);
    if (convertedAnnotation == null) {
      return null;
    }

    return new uk.ac.shef.dcs.sti.core.extension.constraints.Disambiguation(
        convert(e.getPosition()), convertedAnnotation);
  }

  private static uk.ac.shef.dcs.sti.core.extension.annotations.Entity convert(final Entity entity) {
    return new uk.ac.shef.dcs.sti.core.extension.annotations.Entity(entity.getResource(),
        entity.getLabel());
  }

  private static uk.ac.shef.dcs.sti.core.extension.annotations.EntityCandidate convert(
      final EntityCandidate candidate) {
    return new uk.ac.shef.dcs.sti.core.extension.annotations.EntityCandidate(
        convert(candidate.getEntity()), convert(candidate.getScore()));
  }

  private static uk.ac.shef.dcs.sti.core.extension.annotations.HeaderAnnotation convert(
      final HeaderAnnotation e, final KnowledgeBase base) {
    final NavigableSet<EntityCandidate> candidates = e.getCandidates().get(base.getName());
    if (candidates == null) {
      return null;
    }

    final Set<EntityCandidate> chosen = e.getChosen().get(base.getName());
    if (chosen == null) {
      return null;
    }

    return new uk.ac.shef.dcs.sti.core.extension.annotations.HeaderAnnotation(
        convertCandidates(candidates), convertCandidates(chosen));
  }

  private static uk.ac.shef.dcs.sti.core.extension.annotations.Score convert(final Score score) {
    return new uk.ac.shef.dcs.sti.core.extension.annotations.Score(score.getValue());
  }

  private static uk.ac.shef.dcs.sti.core.extension.annotations.StatisticalAnnotation convert(
      final StatisticalAnnotation e, final KnowledgeBase base) {
    final ComponentTypeValue component = e.getComponent().get(base.getName());
    if (component == null) {
      return null;
    }

    final Set<EntityCandidate> predicate = e.getPredicate().get(base.getName());
    if (predicate == null) {
      return null;
    }

    return new uk.ac.shef.dcs.sti.core.extension.annotations.StatisticalAnnotation(
        convert(component), convertCandidates(predicate));
  }

  private static Set<uk.ac.shef.dcs.sti.core.extension.constraints.Ambiguity> convertAmbiguitites(
      final Set<? extends Ambiguity> set) {
    return set.stream().map(DefaultFeedbackToConstraintsAdapter::convert)
        .collect(Collectors.toSet());
  }

  private static Set<uk.ac.shef.dcs.sti.core.extension.annotations.EntityCandidate> convertCandidates(
      final Set<? extends EntityCandidate> set) {
    return set.stream().map(DefaultFeedbackToConstraintsAdapter::convert)
        .collect(Collectors.toSet());
  }

  private static Set<uk.ac.shef.dcs.sti.core.extension.constraints.Classification> convertClassifications(
      final Set<? extends Classification> set, final KnowledgeBase base) {
    return set.stream().map(e -> convert(e, base)).filter(e -> e != null)
        .collect(Collectors.toSet());
  }

  private static Set<uk.ac.shef.dcs.sti.core.extension.constraints.ColumnAmbiguity> convertColumnAmbiguities(
      final Set<? extends ColumnAmbiguity> set) {
    return set.stream().map(DefaultFeedbackToConstraintsAdapter::convert)
        .collect(Collectors.toSet());
  }

  private static Set<uk.ac.shef.dcs.sti.core.extension.constraints.DataCubeComponent> convertDataCubeComponents(
      final Set<? extends DataCubeComponent> set, final KnowledgeBase base) {
    return set.stream().map(e -> convert(e, base)).filter(e -> e != null)
        .collect(Collectors.toSet());
  }

  private static Set<uk.ac.shef.dcs.sti.core.extension.constraints.Disambiguation> convertDisambiguations(
      final Set<? extends Disambiguation> set, final KnowledgeBase base) {
    return set.stream().map(e -> convert(e, base)).filter(e -> e != null)
        .collect(Collectors.toSet());
  }

  private static Set<uk.ac.shef.dcs.sti.core.extension.constraints.ColumnIgnore> convertIgnores(
      final Set<? extends ColumnIgnore> set) {
    return set.stream().map(DefaultFeedbackToConstraintsAdapter::convert)
        .collect(Collectors.toSet());
  }

  private static Set<uk.ac.shef.dcs.sti.core.extension.constraints.ColumnCompulsory> convertCompulsory(
      final Set<? extends ColumnCompulsory> set) {
    return set.stream().map(DefaultFeedbackToConstraintsAdapter::convert)
        .collect(Collectors.toSet());
  }

  private static Set<uk.ac.shef.dcs.sti.core.extension.constraints.ColumnRelation> convertRelations(
      final Set<? extends ColumnRelation> set, final KnowledgeBase base) {
    return set.stream().map(e -> convert(e, base)).filter(e -> e != null)
        .collect(Collectors.toSet());
  }

  private Set<uk.ac.shef.dcs.sti.core.extension.positions.ColumnPosition> convertSubjectColumnsPositions(
      final Feedback feedback, final KnowledgeBase base) {
    final Set<ColumnPosition> subjectColumnsPositions = feedback.getSubjectColumnsPositions().get(base.getName());

    if (subjectColumnsPositions != null) {
      return subjectColumnsPositions.stream()
          .map(DefaultFeedbackToConstraintsAdapter::convert).collect(Collectors.toSet());
    } else {
      return new HashSet<>();
    }
  }

  @Override
  public Constraints toConstraints(final Feedback feedback, final KnowledgeBase base) {
    Preconditions.checkNotNull(feedback);
    Preconditions.checkNotNull(base);

    return new Constraints(
        convertSubjectColumnsPositions(feedback, base),
        convertIgnores(feedback.getColumnIgnores()),
        convertCompulsory(feedback.getColumnCompulsory()),
        convertColumnAmbiguities(feedback.getColumnAmbiguities()),
        convertClassifications(feedback.getClassifications(), base),
        convertRelations(feedback.getColumnRelations(), base),
        convertDisambiguations(feedback.getDisambiguations(), base),
        convertAmbiguitites(feedback.getAmbiguities()),
        convertDataCubeComponents(feedback.getDataCubeComponents(), base));
  }
}
