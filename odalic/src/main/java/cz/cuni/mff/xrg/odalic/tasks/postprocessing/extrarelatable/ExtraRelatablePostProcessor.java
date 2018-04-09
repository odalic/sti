package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable;

import static com.google.common.base.Preconditions.checkArgument;
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
import java.util.stream.IntStream;
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
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnProcessingAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Score;
import cz.cuni.mff.xrg.odalic.tasks.annotations.StatisticalAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.Prefix;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.PrefixMappingService;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.PostProcessor;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.responses.AnnotationReply;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.AnnotationResultValue;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.AnnotationValue;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.ParsedTableValue;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.PropertyValue;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.StatisticsValue;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;



public final class ExtraRelatablePostProcessor implements PostProcessor {

  public static final String ENDPOINT_PARAMETER_KEY = "eu.odalic.extrarelatable.endpoint";
  public static final String LEARN_ANNOTATED_PARAMETER_KEY = "eu.odalic.extrarelatable.learnAnnotated";
  public static final String LANGUAGE_TAG_PARAMETER_KEY = "eu.odalic.extrarelatable.languageTag";
  
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
      final PrefixMappingService prefixMappingService,
      @Nullable final String user,
      final String baseName,
      final Map<String, String> parameters) {
    checkNotNull(inputConverter);
    checkNotNull(prefixMappingService);
    checkNotNull(baseName);
    
    final String endpointValue = parameters.get(ENDPOINT_PARAMETER_KEY);
    checkArgument(endpointValue != null, "The endpoint URI (eu.odalic.extrarelatable.endpoint advanced type property) must be set!");
    final URI endpoint = URI.create(endpointValue);
    
    final String languageTag = parameters.get(LANGUAGE_TAG_PARAMETER_KEY);
    
    this.inputConverter = inputConverter;
    this.prefixMappingService = prefixMappingService;
    this.targetPath = endpoint.resolve(ANNOTATED_SUBPATH);
    this.learnAnnotated = Boolean.parseBoolean(parameters.getOrDefault(LEARN_ANNOTATED_PARAMETER_KEY, Boolean.FALSE.toString()));;
    this.languageTag = languageTag;
    this.user = user;
    this.baseName = baseName;
  }

  @Override
  public Result process(final Input input, final Result result, final Feedback feedback, final String primaryBaseName) {
    // TODO: Detect source columns instead of using only the leftmost one. Use the result and domain of the property?
    // TODO: Labels retrieval.
    
    final ParsedTableValue parsedTable =
        this.inputConverter.convert(input, this.languageTag, this.user);

    final AnnotationResultValue payload = request(parsedTable, AnnotationResultValue.class);

    final Map<Integer, AnnotationValue> extraAnnotations = payload.getAnnotations();
    final Set<Integer> annotatedColumns = payload.getAnnotations().keySet();
    final int columnsCount = input.columnsCount();
    
    return new Result(
        result.getSubjectColumnsPositions(),
        result.getHeaderAnnotations(),
        result.getCellAnnotations(),
        alterColumnRelationAnnotations(result, feedback, primaryBaseName, extraAnnotations,
            result.getSubjectColumnsPositions().get(this.baseName)),
        alterStatisticalAnnotations(result, feedback, extraAnnotations, annotatedColumns, columnsCount),
        alterColumnProcessingAnnotations(result, feedback, annotatedColumns, columnsCount),
        result.getWarnings()
    );
  }

  private Map<ColumnRelationPosition, ColumnRelationAnnotation> alterColumnRelationAnnotations(
      final Result result, final Feedback feedback, final String primaryBaseName,
      final Map<Integer, AnnotationValue> extraAnnotations,
      final Set<? extends ColumnPosition> subjectColumnPostions) {
    final Map<ColumnRelationPosition, ColumnRelationAnnotation> alteredColumnRelationAnnotations =
        new HashMap<>(result.getColumnRelationAnnotations());
    
    //TODO: Implement source index detection here too.
    
    final Map<ColumnRelationPosition, ColumnRelationAnnotation> relationsFeedback = feedback.getColumnRelations().stream().collect(ImmutableMap.toImmutableMap(e -> e.getPosition(), e -> e.getAnnotation()));
    
    for (final ColumnPosition sourceColumnPosition : subjectColumnPostions) {
    
      extraAnnotations.forEach((columnIndex, annotation) -> {
        final ColumnRelationPosition position = new ColumnRelationPosition(sourceColumnPosition.getIndex(), columnIndex);
        
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
          final NavigableSet<EntityCandidate> candidates = IntStream.range(0, annotation.getProperties().size()).mapToObj(index -> toCandidate(annotation.getProperties().get(index), annotation.getPropertiesStatistics().get(index))).collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
          final Set<EntityCandidate> chosen = candidates.isEmpty() ? ImmutableSet.of() : ImmutableSet.of(candidates.last());
          
          alteredCandidates = put(originalCandidates, this.baseName, candidates);
          alteredChosen = put(originalChosen, this.baseName, chosen);
        }
        
        alteredColumnRelationAnnotations.put(position, new ColumnRelationAnnotation(alteredCandidates, alteredChosen));
      });
    
    }
    
    return alteredColumnRelationAnnotations;
  }
  
  private EntityCandidate toCandidate(final PropertyValue property, final StatisticsValue statistics) {
    final String uriString = String.valueOf(property.getUri());
    
    return new EntityCandidate(cz.cuni.mff.xrg.odalic.tasks.annotations.Entity.of(this.prefixMappingService.getPrefix(uriString), uriString, property.getLabels().stream().collect(Collectors.joining("; "))), new Score(1 - statistics.getAverage()));
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
        final AnnotationValue extraAnnotation = extraAnnotations.get(columnIndex);

        final List<PropertyValue> properties = extraAnnotation.getProperties();
        final List<StatisticsValue> stats = extraAnnotation.getPropertiesStatistics();

        final ImmutableSet.Builder<EntityCandidate> candidatesBuilder = ImmutableSet.builder();
        for (int propertyIndex = 0; propertyIndex < properties.size(); propertyIndex++) {
          final PropertyValue property = properties.get(propertyIndex);

          final String resourceId = String.valueOf(property.getUri());
          final Prefix prefix = this.prefixMappingService.getPrefix(resourceId);

          final cz.cuni.mff.xrg.odalic.tasks.annotations.Entity entity =
              cz.cuni.mff.xrg.odalic.tasks.annotations.Entity.of(prefix, resourceId, "");

          final StatisticsValue stat = stats.get(propertyIndex);

          candidatesBuilder.add(new EntityCandidate(entity, new Score(stat.getAverage())));
        }

        componentType = ComponentTypeValue.DIMENSION;
        candidates = candidatesBuilder.build();
      } else {
        componentType = ComponentTypeValue.DIMENSION;
        candidates = ImmutableSet.of();
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

  private static <K, V> Map<K, V> put(final Map<K, V> map, final K key, final V value) {
    final Map<K, V> result = new HashMap<>(map);
    result.put(key, value);
    
    return result;
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

    final MediaType responseMediaType = response.getMediaType();
    
    if (responseMediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
      final AnnotationReply reply = response.readEntity(AnnotationReply.class);
      
      return responseType.cast(reply.getPayload()); // TODO: Generalize. 
    } else {
      throw new RuntimeException(response.getStatusInfo().getReasonPhrase());
    }
  }

  private static boolean isSuccessful(final Response response) {
    return response.getStatusInfo().getFamily() == Family.SUCCESSFUL;
  }
}
