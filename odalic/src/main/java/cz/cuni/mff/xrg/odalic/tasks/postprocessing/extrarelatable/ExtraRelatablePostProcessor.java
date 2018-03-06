package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable;

import static com.google.common.base.Preconditions.checkNotNull;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnProcessingTypeValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.ComponentTypeValue;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnCompulsory;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnIgnore;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.positions.CellPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnProcessingAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Score;
import cz.cuni.mff.xrg.odalic.tasks.annotations.StatisticalAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.Prefix;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.PrefixMappingService;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.PostProcessor;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.responses.Reply;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.AnnotationResultValue;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.AnnotationValue;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.ParsedTableValue;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.PropertyValue;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.StatisticsValue;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;
import cz.cuni.mff.xrg.odalic.util.Arrays;



public final class ExtraRelatablePostProcessor implements PostProcessor {

  private static final URI ANNOTATED_SUBPATH = URI.create("annotated");
  private static final String LEARN_QUERY_PARAMETER_NAME = "learn";

  private final InputConverter inputConverter;
  private final PrefixMappingService prefixMappingService;
  private final URI targetPath;
  private final boolean learnAnnotated;
  private final String languageTag;
  private final String user;
  private final String baseName;

  public ExtraRelatablePostProcessor(final InputConverter inputConverter,
      final PrefixMappingService prefixMappingService, final URI baseTargetPath,
      final URI graphName, final boolean learnAnnotated, @Nullable final String languageTag,
      @Nullable final String user) {
    checkNotNull(inputConverter);
    checkNotNull(prefixMappingService);
    checkNotNull(baseTargetPath);
    checkNotNull(graphName);

    this.inputConverter = inputConverter;
    this.prefixMappingService = prefixMappingService;
    this.targetPath = baseTargetPath.resolve(graphName).resolve(ANNOTATED_SUBPATH);
    this.learnAnnotated = learnAnnotated;
    this.languageTag = languageTag;
    this.user = user;
    this.baseName = graphName.toString();
  }

  @Override
  public Result process(final Input input, final Result result, final Feedback feedback, final String primaryBaseName) {
    // TODO: Detect source columns instead of using only the leftmost one. Use the result and domain of the property?
    // TODO: Labels retrieval.
    
    final ParsedTableValue parsedTable =
        this.inputConverter.convert(input, this.languageTag, this.user);

    final Reply reply = request(parsedTable, Reply.class);

    final AnnotationResultValue payload = reply.getPayload();

    final Map<Integer, AnnotationValue> extraAnnotations = payload.getAnnotations();
    final Set<Integer> annotatedColumns = payload.getAnnotations().keySet();
    final int columnsCount = input.columnsCount();

    final Map<String, Set<ColumnPosition>> alteredSubjectColumnPostions =
        alterSubjectColumnPositions(result, feedback, primaryBaseName);
    
    return new Result(
        alteredSubjectColumnPostions,
        alterClassifications(result, feedback),
        alterCellAnnotations(input, result, feedback),
        alterColumnRelationAnnotations(result, feedback, primaryBaseName, extraAnnotations,
            alteredSubjectColumnPostions),
        alterStatisticalAnnotations(result, feedback, extraAnnotations, annotatedColumns, columnsCount),
        alterColumnProcessingAnnotations(result, feedback, annotatedColumns, columnsCount),
        result.getWarnings()
    );
  }

  private Map<ColumnRelationPosition, ColumnRelationAnnotation> alterColumnRelationAnnotations(
      final Result result, final Feedback feedback, final String primaryBaseName,
      final Map<Integer, AnnotationValue> extraAnnotations,
      final Map<String, Set<ColumnPosition>> alteredSubjectColumnPostions) {
    final Map<ColumnRelationPosition, ColumnRelationAnnotation> alteredColumnRelationAnnotations =
        new HashMap<>(result.getColumnRelationAnnotations());
    
    final int sourceColumnIndex = alteredSubjectColumnPostions.get(primaryBaseName).stream().sorted().findFirst().get().getIndex();
    
    final Map<ColumnRelationPosition, ColumnRelationAnnotation> relationsFeedback = feedback.getColumnRelations().stream().collect(ImmutableMap.toImmutableMap(e -> e.getPosition(), e -> e.getAnnotation()));
    
    extraAnnotations.forEach((columnIndex, annotation) -> {
      final ColumnRelationPosition position = new ColumnRelationPosition(sourceColumnIndex, columnIndex);
      
      final ColumnRelationAnnotation originalAnnotation = alteredColumnRelationAnnotations.get(position);
      
      final Map<String, NavigableSet<EntityCandidate>> originalCandidates;
      final Map<String, Set<EntityCandidate>> originalChosen;
      if (originalAnnotation != null) {
        originalCandidates = originalAnnotation.getCandidates();
        originalChosen = originalAnnotation.getChosen();        
      } else {
        originalCandidates = ImmutableMap.of();
        originalChosen = ImmutableMap.of();
      }
        
      final Map<String, NavigableSet<EntityCandidate>> alteredCandidates;
      final Map<String, Set<EntityCandidate>> alteredChosen;
      
      final ColumnRelationAnnotation feedbackAnnotation = relationsFeedback.get(position);
      
      if (feedbackAnnotation != null) {
        final NavigableSet<EntityCandidate> feedbackCandidates = feedbackAnnotation.getCandidates().get(this.baseName);
        final Set<EntityCandidate> feedbackChosen = feedbackAnnotation.getChosen().get(this.baseName);
        
        if (feedbackCandidates == null) {
          alteredCandidates = originalCandidates;
        } else {
          alteredCandidates = put(originalCandidates, this.baseName, feedbackCandidates);
        }
        
        if (feedbackChosen == null) {
          alteredChosen = originalChosen;
        } else {
          alteredChosen = put(originalChosen, this.baseName, feedbackChosen);
        }
      } else {
        final NavigableSet<EntityCandidate> candidates = annotation.getProperties().stream().map(property -> toCandidate(property, annotation.getPropertiesStatistics().get(columnIndex))).collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
        final Set<EntityCandidate> chosen = candidates.isEmpty() ? ImmutableSet.of() : ImmutableSet.of(candidates.last());
        
        alteredCandidates = put(originalCandidates, this.baseName, candidates);
        alteredChosen = put(originalChosen, this.baseName, chosen);
      }
      
      alteredColumnRelationAnnotations.put(position, new ColumnRelationAnnotation(alteredCandidates, alteredChosen));
    });
    return alteredColumnRelationAnnotations;
  }
  
  private EntityCandidate toCandidate(final PropertyValue property, final StatisticsValue statistics) {
    final String uriString = property.getUri().toString();
    
    return new EntityCandidate(cz.cuni.mff.xrg.odalic.tasks.annotations.Entity.of(this.prefixMappingService.getPrefix(uriString), uriString, ""), new Score(statistics.getAverage()));
  }

  private Map<String, Set<ColumnPosition>> alterSubjectColumnPositions(final Result result,
      final Feedback feedback, final String primaryBaseName) {
    final Map<String, Set<ColumnPosition>> alteredSubjectColumnPostions = new HashMap<>(result.getSubjectColumnsPositions());
    alteredSubjectColumnPostions.put(this.baseName, alteredSubjectColumnPostions.get(primaryBaseName)); // Use same as the primary.
    final Set<ColumnPosition> feedbackSubjectColumnPositions = feedback.getSubjectColumnsPositions().get(this.baseName);
    if (feedbackSubjectColumnPositions != null) {
      alteredSubjectColumnPostions.put(this.baseName, feedbackSubjectColumnPositions);
    }
    return alteredSubjectColumnPostions;
  }

  private List<HeaderAnnotation> alterClassifications(final Result result,
      final Feedback feedback) {
    final List<HeaderAnnotation> alteredHeaderAnnotations = result.getHeaderAnnotations().stream()
        .map(annotation -> new HeaderAnnotation(
            put(annotation.getCandidates(), this.baseName, ImmutableSortedSet.of()),
            put(annotation.getChosen(), this.baseName, ImmutableSet.of())))
        .collect(Collectors.toCollection(ArrayList::new));
    feedback.getClassifications().forEach(classification -> {
      final HeaderAnnotation originalAnnotation = alteredHeaderAnnotations.get(classification.getPosition().getIndex());
      
      final HeaderAnnotation feedbackAnnotation = classification.getAnnotation();
      final NavigableSet<EntityCandidate> feedbackCandidates = feedbackAnnotation.getCandidates().get(this.baseName);
      final Set<EntityCandidate> feedbackChosen = feedbackAnnotation.getChosen().get(this.baseName);
      
      final Map<String, NavigableSet<EntityCandidate>> originalCandidates = originalAnnotation.getCandidates();
      final Map<String, Set<EntityCandidate>> originalChosen = originalAnnotation.getChosen();
      
      final Map<String, NavigableSet<EntityCandidate>> alteredCandidates;
      final Map<String, Set<EntityCandidate>> alteredChosen;
      if (feedbackCandidates == null) {
        alteredCandidates = originalCandidates;
      } else {
        alteredCandidates = put(originalCandidates, this.baseName, feedbackCandidates);
      }
      
      if (feedbackChosen == null) {
        alteredChosen = originalChosen;
      } else {
        alteredChosen = put(originalChosen, this.baseName, feedbackChosen);
      }
      
      alteredHeaderAnnotations.set(classification.getPosition().getIndex(), new HeaderAnnotation(alteredCandidates, alteredChosen));
    });
    return alteredHeaderAnnotations;
  }

  private CellAnnotation[][] alterCellAnnotations(final Input input, final Result result,
      final Feedback feedback) {
    final CellAnnotation[][] alteredCellAnnotations =
        Arrays.deepCopy(CellAnnotation.class, result.getCellAnnotations());
    for (int rowIndex = 0; rowIndex < input.rowsCount(); rowIndex++) {
      for (int columnIndex = 0; columnIndex < input.columnsCount(); columnIndex++) {
        final CellAnnotation originalCellAnnotation = alteredCellAnnotations[rowIndex][columnIndex];

        alteredCellAnnotations[rowIndex][columnIndex] = new CellAnnotation(
            put(originalCellAnnotation.getCandidates(), this.baseName, ImmutableSortedSet.of()),
            put(originalCellAnnotation.getChosen(), this.baseName, ImmutableSet.of()));
      }
    }
    feedback.getDisambiguations().forEach(disambiguation -> {
      final CellPosition position = disambiguation.getPosition();
      
      final CellAnnotation originalAnnotation = alteredCellAnnotations[position.getRowIndex()][position.getColumnIndex()];
      
      final CellAnnotation feedbackAnnotation = disambiguation.getAnnotation();
      final NavigableSet<EntityCandidate> feedbackCandidates = feedbackAnnotation.getCandidates().get(this.baseName);
      final Set<EntityCandidate> feedbackChosen = feedbackAnnotation.getChosen().get(this.baseName);
      
      final Map<String, NavigableSet<EntityCandidate>> originalCandidates = originalAnnotation.getCandidates();
      final Map<String, Set<EntityCandidate>> originalChosen = originalAnnotation.getChosen();
      
      final Map<String, NavigableSet<EntityCandidate>> alteredCandidates;
      final Map<String, Set<EntityCandidate>> alteredChosen;
      if (feedbackCandidates == null) {
        alteredCandidates = originalCandidates;
      } else {
        alteredCandidates = put(originalCandidates, this.baseName, feedbackCandidates);
      }
      
      if (feedbackChosen == null) {
        alteredChosen = originalChosen;
      } else {
        alteredChosen = put(originalChosen, this.baseName, feedbackChosen);
      }
      
      alteredCellAnnotations[position.getRowIndex()][position.getColumnIndex()] = new CellAnnotation(alteredCandidates, alteredChosen);
    });
    feedback.getAmbiguities().forEach(ambiguity -> {
      final CellPosition position = ambiguity.getPosition();
      
      final CellAnnotation originalAnnotation = alteredCellAnnotations[position.getRowIndex()][position.getColumnIndex()];
      
      final Map<String, NavigableSet<EntityCandidate>> originalCandidates = originalAnnotation.getCandidates();
      final Map<String, Set<EntityCandidate>> originalChosen = originalAnnotation.getChosen();
      
      final Map<String, NavigableSet<EntityCandidate>> alteredCandidates = put(originalCandidates, this.baseName, ImmutableSortedSet.of());
      final Map<String, Set<EntityCandidate>> alteredChosen = put(originalChosen, this.baseName, ImmutableSet.of());
      
      alteredCellAnnotations[position.getRowIndex()][position.getColumnIndex()] = new CellAnnotation(alteredCandidates, alteredChosen);
    });
    feedback.getColumnAmbiguities().forEach(columnAmbiguity -> {
      final int columnIndex = columnAmbiguity.getPosition().getIndex();
      
      for (int rowIndex = 0; rowIndex < input.rowsCount(); rowIndex++) {
        final CellAnnotation originalAnnotation = alteredCellAnnotations[rowIndex][columnIndex];
        
        final Map<String, NavigableSet<EntityCandidate>> originalCandidates = originalAnnotation.getCandidates();
        final Map<String, Set<EntityCandidate>> originalChosen = originalAnnotation.getChosen();
        
        final Map<String, NavigableSet<EntityCandidate>> alteredCandidates = put(originalCandidates, this.baseName, ImmutableSortedSet.of());
        final Map<String, Set<EntityCandidate>> alteredChosen = put(originalChosen, this.baseName, ImmutableSet.of());
        
        alteredCellAnnotations[rowIndex][columnIndex] = new CellAnnotation(alteredCandidates, alteredChosen);
      }
    });
    return alteredCellAnnotations;
  }

  private List<StatisticalAnnotation> alterStatisticalAnnotations(final Result result,
      final Feedback feedback, final Map<Integer, AnnotationValue> extraAnnotations,
      final Set<Integer> annotatedColumns, final int columnsCount) {
    final List<StatisticalAnnotation> originalStatisticalAnnotations =
        result.getStatisticalAnnotations();
    final List<StatisticalAnnotation> alteredStatisticalAnnotations =
        new ArrayList<>(originalStatisticalAnnotations.size());
    for (int columnIndex = 0; columnIndex < columnsCount; columnIndex++) {
      final StatisticalAnnotation originalAnnotation =
          originalStatisticalAnnotations.get(columnIndex);

      final boolean annotated = annotatedColumns.contains(columnIndex);

      final ComponentTypeValue componentType;
      final Set<EntityCandidate> candidates;
      if (annotated) {
        componentType = ComponentTypeValue.MEASURE;
        candidates = ImmutableSet.of();
      } else {
        final AnnotationValue extraAnnotation = extraAnnotations.get(columnIndex);

        final List<PropertyValue> properties = extraAnnotation.getProperties();
        final List<StatisticsValue> stats = extraAnnotation.getPropertiesStatistics();

        final ImmutableSet.Builder<EntityCandidate> candidatesBuilder = ImmutableSet.builder();
        for (int propertyIndex = 0; propertyIndex < properties.size(); propertyIndex++) {
          final PropertyValue property = properties.get(propertyIndex);

          final String resourceId = property.getUri().toString();
          final Prefix prefix = this.prefixMappingService.getPrefix(resourceId);

          final cz.cuni.mff.xrg.odalic.tasks.annotations.Entity entity =
              cz.cuni.mff.xrg.odalic.tasks.annotations.Entity.of(prefix, resourceId, "");

          final StatisticsValue stat = stats.get(propertyIndex);

          candidatesBuilder.add(new EntityCandidate(entity, new Score(stat.getAverage())));
        }

        componentType = ComponentTypeValue.DIMENSION;
        candidates = candidatesBuilder.build();
      }

      final StatisticalAnnotation alteredAnnotation = new StatisticalAnnotation(
          put(originalAnnotation.getComponent(), this.baseName, componentType),
          put(originalAnnotation.getPredicate(), this.baseName, candidates));
      alteredStatisticalAnnotations.add(alteredAnnotation);
    }
    feedback.getDataCubeComponents().forEach(component -> {
      final StatisticalAnnotation originalAnnotation = alteredStatisticalAnnotations.get(component.getPosition().getIndex());
      final StatisticalAnnotation feedbackAnnotation = component.getAnnotation();
      
      final ComponentTypeValue feedbackComponentType = feedbackAnnotation.getComponent().get(this.baseName);
      final Set<EntityCandidate> feedbackPredicate = feedbackAnnotation.getPredicate().get(this.baseName);
      
      final Map<String, ComponentTypeValue> originalTypes = originalAnnotation.getComponent();
      final Map<String, Set<EntityCandidate>> originalPredicate = originalAnnotation.getPredicate();
      
      final Map<String, ComponentTypeValue> alteredTypes;
      final Map<String, Set<EntityCandidate>> alteredPredicate;
      if (feedbackComponentType == null) {
        alteredTypes = originalTypes;
      } else {
        alteredTypes = put(originalTypes, this.baseName, feedbackComponentType);
      }
      
      if (feedbackPredicate == null) {
        alteredPredicate = originalPredicate;
      } else {
        alteredPredicate = put(originalPredicate, this.baseName, feedbackPredicate);
      }
      
      alteredStatisticalAnnotations.set(component.getPosition().getIndex(), new StatisticalAnnotation(alteredTypes, alteredPredicate));
    });
    return alteredStatisticalAnnotations;
  }

  private List<ColumnProcessingAnnotation> alterColumnProcessingAnnotations(final Result result,
      final Feedback feedback, final Set<Integer> annotatedColumns, final int columnsCount) {
    final List<ColumnProcessingAnnotation> originalColumnProcessingAnnotations =
        result.getColumnProcessingAnnotations();
    final ImmutableList.Builder<ColumnProcessingAnnotation> alteredColumnProcessingAnnotationsBuilder =
        ImmutableList.builder();
    for (int columnIndex = 0; columnIndex < columnsCount; columnIndex++) {
      final ColumnProcessingAnnotation originalAnnotation =
          originalColumnProcessingAnnotations.get(columnIndex);

      final ColumnPosition columnPosition = new ColumnPosition(columnIndex);

      final ColumnProcessingTypeValue processingType;
      if (feedback.getColumnCompulsory().contains(new ColumnCompulsory(columnPosition))) {
        processingType = ColumnProcessingTypeValue.COMPULSORY;
      } else if (feedback.getColumnIgnores().contains(new ColumnIgnore(columnPosition))) {
        processingType = ColumnProcessingTypeValue.IGNORED;
      } else {
        processingType =
            annotatedColumns.contains(columnIndex) ? ColumnProcessingTypeValue.NON_NAMED_ENTITY
                : ColumnProcessingTypeValue.NAMED_ENTITY;
      }

      final ColumnProcessingAnnotation alteredAnnotation = new ColumnProcessingAnnotation(
          put(originalAnnotation.getProcessingType(), this.baseName, processingType));

      alteredColumnProcessingAnnotationsBuilder.add(alteredAnnotation);
    }
    
    return alteredColumnProcessingAnnotationsBuilder.build();
  }

  private <K, V> Map<K, V> put(final Map<K, V> map, final K key, final V value) {
    return ImmutableMap.<K, V>builder().putAll(map).put(key, value).build();
  }

  private <T, U> U request(final T requestEntity, final Class<? extends U> responseType) {
    final Client client = ClientBuilder.newBuilder().build();

    final WebTarget target =
        client.target(this.targetPath).queryParam(LEARN_QUERY_PARAMETER_NAME, learnAnnotated);

    final Response response = target.request().accept(MediaType.APPLICATION_JSON_TYPE)
        .post(Entity.entity(requestEntity, MediaType.APPLICATION_JSON_TYPE));

    if (!isSuccessful(response)) {
      throw new IllegalStateException("The request failed: " + response.getStatus() + "["
          + response.readEntity(String.class) + "]");
    }

    if (response.getMediaType() == MediaType.APPLICATION_JSON_TYPE) {
      return response.readEntity(responseType);
    } else {
      throw new RuntimeException(response.getStatusInfo().getReasonPhrase());
    }
  }

  private static boolean isSuccessful(final Response response) {
    return response.getStatusInfo().getFamily() == Family.SUCCESSFUL;
  }
}
