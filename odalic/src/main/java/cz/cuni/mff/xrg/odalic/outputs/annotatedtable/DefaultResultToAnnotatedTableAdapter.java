package cz.cuni.mff.xrg.odalic.outputs.annotatedtable;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rest.values.ComponentTypeValue;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.bases.proxies.KnowledgeBaseProxiesService;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.Prefix;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;
import uk.ac.shef.dcs.kbproxy.ProxyException;
import uk.ac.shef.dcs.kbproxy.Proxy;

/**
 * The default {@link ResultToAnnotatedTableAdapter} implementation.
 *
 * @author Josef Janoušek
 *
 */
public class DefaultResultToAnnotatedTableAdapter implements ResultToAnnotatedTableAdapter {

  private static final Logger log =
      LoggerFactory.getLogger(DefaultResultToAnnotatedTableAdapter.class);

  private static final Prefix PREFIX_RDF = Prefix.create(RDF.PREFIX, RDF.NAMESPACE);

  private static final Entity RDF_TYPE = Entity.of(PREFIX_RDF, RDF.TYPE.stringValue(), "");
  private static final Entity RDF_PROPERTY = Entity.of(PREFIX_RDF, RDF.PROPERTY.stringValue(), "");
  private static final Prefix PREFIX_OWL = Prefix.create(OWL.PREFIX, OWL.NAMESPACE);
  private static final Entity OWL_SAMEAS = Entity.of(PREFIX_OWL, OWL.SAMEAS.stringValue(), "");
  private static final Prefix PREFIX_DCTERMS = Prefix.create(DCTERMS.PREFIX, DCTERMS.NAMESPACE);

  private static final Entity DCTERMS_TITLE =
      Entity.of(PREFIX_DCTERMS, DCTERMS.TITLE.stringValue(), "");

  private static final Prefix PREFIX_RDFS = Prefix.create(RDFS.PREFIX, RDFS.NAMESPACE);

  private static final Entity RDFS_LABEL = Entity.of(PREFIX_RDFS, RDFS.LABEL.stringValue(), "");
  private static final Entity RDFS_SUBPROPERTYOF =
      Entity.of(PREFIX_RDFS, RDFS.SUBPROPERTYOF.stringValue(), "");
  private static final Prefix PREFIX_XSD = Prefix.create(XMLSchema.PREFIX, XMLSchema.NAMESPACE);

  private static final Entity XSD_STRING =
      Entity.of(PREFIX_XSD, XMLSchema.STRING.stringValue(), "");
  private static final Entity XSD_ANYURI =
      Entity.of(PREFIX_XSD, XMLSchema.ANYURI.stringValue(), "");

  private static final String QB = "http://purl.org/linked-data/cube#";
  private static final Prefix PREFIX_QB = Prefix.create("qb", QB);

  private static final Entity QB_DATASET = Entity.of(PREFIX_QB, QB + "DataSet", "");
  private static final Entity QB_STRUCTURE = Entity.of(PREFIX_QB, QB + "structure", "");
  private static final Entity QB_DATASTRUCTUREDEFINITION =
      Entity.of(PREFIX_QB, QB + "DataStructureDefinition", "");

  private static final Entity QB_OBSERVATION = Entity.of(PREFIX_QB, QB + "Observation", "");
  private static final Entity QB_DATASET_PRED = Entity.of(PREFIX_QB, QB + "dataSet", "");
  private static final Entity QB_COMPONENT = Entity.of(PREFIX_QB, QB + "component", "");

  private static final Entity QB_DIMENSION = Entity.of(PREFIX_QB, QB + "dimension", "");
  private static final Entity QB_MEASURE = Entity.of(PREFIX_QB, QB + "measure", "");
  private static final Entity QB_DIMENSIONPROPERTY =
      Entity.of(PREFIX_QB, QB + "DimensionProperty", "");
  private static final Entity QB_MEASUREPROPERTY = Entity.of(PREFIX_QB, QB + "MeasureProperty", "");
  private static final Entity QB_CONCEPT = Entity.of(PREFIX_QB, QB + "concept", "");
  private static final Prefix PREFIX_SDMX_MEASURE =
      Prefix.create("sdmx-measure", "http://purl.org/linked-data/sdmx/2009/measure#");
  private static final Entity SDMX_MEASURE_OBSVALUE =
      Entity.of(PREFIX_SDMX_MEASURE, PREFIX_SDMX_MEASURE.getWhat() + "obsValue", "");
  private static final String SEPARATOR = " ";
  private static final String OBSERVATION = "OBSERVATION";
  private final KnowledgeBaseProxiesService knowledgeBaseProxyFactory;
  private final TableColumnBuilder builder = new TableColumnBuilder();
  private List<String> headers;
  private boolean[] isDisambiguated;

  private Proxy kbProxy;
  private Map<String, String> prefixes;

  @Autowired
  public DefaultResultToAnnotatedTableAdapter(
      final KnowledgeBaseProxiesService knowledgeBaseProxyFactory) {
    Preconditions.checkNotNull(knowledgeBaseProxyFactory);

    this.knowledgeBaseProxyFactory = knowledgeBaseProxyFactory;
  }

  private String alternativeUrlsFormat(final String text) {
    return String.format("%s_alternative_urls", text);
  }

  private String bracketFormat(final String text) {
    return String.format("{%s}", text);
  }

  private TableColumn createAlternativeDisambiguationColumn(final int columnIndex) {
    final String columnName = this.headers.get(columnIndex);

    this.builder.clear();
    this.builder.setName(alternativeUrlsFormat(columnName));
    this.builder.setAboutUrl(bracketFormat(urlFormat(columnName)));
    this.builder.setSeparator(SEPARATOR);
    this.builder.setPropertyUrl(OWL_SAMEAS.getPrefixed());
    this.builder.setValueUrl(bracketFormat(alternativeUrlsFormat(columnName)));

    return this.builder.build();
  }

  private TableColumn createClassificationColumn(final int subjectColumnIndex,
      final Entity object) {
    return createPredColumn(typeFormat(this.headers.get(subjectColumnIndex)), subjectColumnIndex,
        RDF_TYPE, object);
  }

  private TableColumn createDisambiguationColumn(final int columnIndex) {
    final String columnName = this.headers.get(columnIndex);

    this.builder.clear();
    this.builder.setName(urlFormat(columnName));
    this.builder.setDataType(XSD_ANYURI.getPrefixed());
    this.builder.setSuppressOutput(true);
    this.builder.setValueUrl(bracketFormat(urlFormat(columnName)));

    return this.builder.build();
  }

  private TableColumn createOriginalColumn(final int columnIndex) {
    final String columnName = this.headers.get(columnIndex);

    this.builder.clear();
    this.builder.setName(columnName);
    this.builder.setTitles(Arrays.asList(columnName));
    this.builder.setDataType(XSD_STRING.getPrefixed());

    if (this.isDisambiguated[columnIndex]) {
      this.builder.setAboutUrl(bracketFormat(urlFormat(columnName)));
      this.builder.setPropertyUrl(DCTERMS_TITLE.getPrefixed());
    }

    return this.builder.build();
  }

  private TableColumn createPredColumn(final String name, final int subjectColumnIndex,
      final Entity predicate, final Entity object) {
    putPrefix(object.getPrefix());

    return createVirtualColumn(name, bracketFormat(urlFormat(this.headers.get(subjectColumnIndex))),
        predicate, object.getPrefixed());
  }

  private TableColumn createRelationColumn(final int subjectColumnIndex, final Entity predicate,
      final int objectColumnIndex) {
    final String subjectColumnName = this.headers.get(subjectColumnIndex);
    final String objectColumnName = this.headers.get(objectColumnIndex);

    putPrefix(predicate.getPrefix());

    this.builder.clear();
    this.builder.setName(predicate.getResource());
    this.builder.setVirtual(true);
    this.builder.setAboutUrl(bracketFormat(urlFormat(subjectColumnName)));
    this.builder.setPropertyUrl(predicate.getPrefixed());

    if (this.isDisambiguated[objectColumnIndex]) {
      this.builder.setValueUrl(bracketFormat(urlFormat(objectColumnName)));
    } else {
      this.builder.setValueUrl(bracketFormat(objectColumnName));
      List<String> ranges;
      try {
        ranges = this.kbProxy.getPropertyRanges(predicate.getResource());
      } catch (final ProxyException e) {
        log.warn("Ranges not found for predicate " + predicate.getResource(), e);
        ranges = new ArrayList<>();
      }
      if (!ranges.isEmpty()) {
        Entity dataType;
        if (this.knowledgeBaseProxyFactory.getPrefixService() != null) {
          dataType =
              Entity.of(this.knowledgeBaseProxyFactory.getPrefixService().getPrefix(ranges.get(0)),
                  ranges.get(0), "");
          putPrefix(dataType.getPrefix());
        } else {
          dataType = Entity.of(ranges.get(0), "");
        }

        this.builder.setDataType(dataType.getPrefixed());
      }
    }

    return this.builder.build();
  }

  private TableColumn createTripleColumn(final String name, final Entity subject,
      final Entity predicate, final Entity object) {
    putPrefix(object.getPrefix());

    return createTripleColumn(name, subject, predicate, object.getPrefixed());
  }

  private TableColumn createTripleColumn(final String name, final Entity subject,
      final Entity predicate, final String object) {
    putPrefix(subject.getPrefix());

    return createVirtualColumn(name, subject.getPrefixed(), predicate, object);
  }

  private TableColumn createVirtualColumn(final String name, final String subject,
      final Entity predicate, final String object) {
    putPrefix(predicate.getPrefix());

    this.builder.clear();
    this.builder.setName(name);
    this.builder.setVirtual(true);
    this.builder.setAboutUrl(subject);
    this.builder.setPropertyUrl(predicate.getPrefixed());
    this.builder.setValueUrl(object);

    return this.builder.build();
  }

  private String generateStringUUID() {
    return UUID.randomUUID().toString();
  }

  private void putPrefix(final Prefix prefix) {
    if ((prefix != null) && !this.prefixes.containsKey(prefix.getWith())) {
      this.prefixes.put(prefix.getWith(), prefix.getWhat());
    }
  }

  @Override
  public AnnotatedTable toAnnotatedTable(final Result result, final Input input,
      final boolean statistical, final KnowledgeBase primaryBase) {

    this.headers = new ArrayList<>(input.headers());
    if (statistical) {
      this.headers.add(OBSERVATION);
    }

    this.isDisambiguated = new boolean[this.headers.size()];
    this.kbProxy =
        this.knowledgeBaseProxyFactory.toProxies(ImmutableSet.of(primaryBase)).values().iterator().next();

    this.prefixes = new HashMap<>();
    putPrefix(PREFIX_XSD);
    putPrefix(PREFIX_OWL);
    putPrefix(PREFIX_DCTERMS);

    final List<TableColumn> columns = new ArrayList<TableColumn>();

    for (int i = 0; i < input.columnsCount(); i++) {
      boolean addPrimary = false;
      boolean addAlternatives = false;

      for (int j = 0; j < input.rowsCount(); j++) {
        for (final Entry<String, Set<EntityCandidate>> entry : result
            .getCellAnnotations()[j][i].getChosen().entrySet()) {
          if ((entry.getValue() != null) && !entry.getValue().isEmpty()) {
            if (entry.getKey().equals(primaryBase)) {
              addPrimary = true;
            } else {
              addAlternatives = true;
            }
          }
        }

        if (addPrimary && addAlternatives) {
          break;
        }
      }

      this.isDisambiguated[i] = addPrimary || addAlternatives;

      columns.add(createOriginalColumn(i));

      if (this.isDisambiguated[i]) {
        columns.add(createDisambiguationColumn(i));
      }

      if (addAlternatives) {
        columns.add(createAlternativeDisambiguationColumn(i));
      }

      for (final Set<EntityCandidate> set : result.getHeaderAnnotations().get(i).getChosen()
          .values()) {
        if ((set != null) && !set.isEmpty()) {
          for (final EntityCandidate chosen : set) {
            columns.add(createClassificationColumn(i, chosen.getEntity()));
          }
        }
      }
    }

    for (final Entry<ColumnRelationPosition, ColumnRelationAnnotation> entry : result
        .getColumnRelationAnnotations().entrySet()) {
      final Set<EntityCandidate> chosenRelations =
          entry.getValue().getChosen().get(primaryBase);

      if (chosenRelations != null) {
        for (final EntityCandidate chosen : chosenRelations) {
          columns.add(createRelationColumn(entry.getKey().getFirstIndex(), chosen.getEntity(),
              entry.getKey().getSecondIndex()));
        }
      }
    }

    if (statistical) {
      final int obsIndex = this.headers.size() - 1;
      this.isDisambiguated[obsIndex] = true;

      final URI kbUri = primaryBase.getUserClassesPrefix();
      final Entity datasetEntity =
          Entity.of(String.format("%sdataset/%s", kbUri, generateStringUUID()), "");
      final Entity dsdEntity =
          Entity.of(String.format("%sdsd/%s", kbUri, generateStringUUID()), "");

      // dataset definition
      columns.add(createTripleColumn(typeFormat("dataset"), datasetEntity, RDF_TYPE, QB_DATASET));
      columns.add(
          createTripleColumn("dataset_title", datasetEntity, DCTERMS_TITLE, input.identifier()));
      columns.add(createTripleColumn("dataset_structure", datasetEntity, QB_STRUCTURE, dsdEntity));

      // data structure definition
      columns.add(
          createTripleColumn(typeFormat("dsd"), dsdEntity, RDF_TYPE, QB_DATASTRUCTUREDEFINITION));

      // observation definition
      columns.add(createDisambiguationColumn(obsIndex));
      columns.add(createClassificationColumn(obsIndex, QB_OBSERVATION));
      columns.add(
          createPredColumn(OBSERVATION + "_dataset", obsIndex, QB_DATASET_PRED, datasetEntity));

      Entity compEntity;
      for (int i = 0; i < input.columnsCount(); i++) {
        final Set<EntityCandidate> predicateSet = result.getStatisticalAnnotations().get(i)
            .getPredicate().get(primaryBase);

        if ((predicateSet != null) && !predicateSet.isEmpty()) {
          final ComponentTypeValue componentType = result.getStatisticalAnnotations().get(i)
              .getComponent().get(primaryBase);

          final Entity predicateEntity = predicateSet.iterator().next().getEntity();

          final Set<EntityCandidate> classificationSet =
              result.getHeaderAnnotations().get(i).getChosen().get(primaryBase);

          switch (componentType) {
            case DIMENSION:
              // component definition
              compEntity =
                  Entity.of(String.format("%sdimension/%s", kbUri, generateStringUUID()), "");
              columns.add(createTripleColumn("dsd_component", dsdEntity, QB_COMPONENT, compEntity));
              columns.add(
                  createTripleColumn("component_kind", compEntity, QB_DIMENSION, predicateEntity));

              // dimension definition
              columns.add(createTripleColumn(typeFormat(predicateEntity.getResource()),
                  predicateEntity, RDF_TYPE, RDF_PROPERTY));
              columns.add(createTripleColumn(typeFormat(predicateEntity.getResource()),
                  predicateEntity, RDF_TYPE, QB_DIMENSIONPROPERTY));
              columns.add(createTripleColumn(predicateEntity.getResource() + "_label",
                  predicateEntity, RDFS_LABEL, predicateEntity.getLabel()));
              if ((classificationSet != null) && !classificationSet.isEmpty()) {
                final Entity classificationEntity = classificationSet.iterator().next().getEntity();
                columns.add(createTripleColumn(predicateEntity.getResource() + "_concept",
                    predicateEntity, QB_CONCEPT, classificationEntity));
              }

              // observation relations
              columns.add(createRelationColumn(obsIndex, predicateEntity, i));
              break;
            case MEASURE:
              // component definition
              compEntity =
                  Entity.of(String.format("%smeasure/%s", kbUri, generateStringUUID()), "");
              columns.add(createTripleColumn("dsd_component", dsdEntity, QB_COMPONENT, compEntity));
              columns.add(
                  createTripleColumn("component_kind", compEntity, QB_MEASURE, predicateEntity));

              // measure definition
              columns.add(createTripleColumn(typeFormat(predicateEntity.getResource()),
                  predicateEntity, RDF_TYPE, RDF_PROPERTY));
              columns.add(createTripleColumn(typeFormat(predicateEntity.getResource()),
                  predicateEntity, RDF_TYPE, QB_MEASUREPROPERTY));
              columns.add(createTripleColumn(predicateEntity.getResource() + "_label",
                  predicateEntity, RDFS_LABEL, predicateEntity.getLabel()));
              columns.add(createTripleColumn(predicateEntity.getResource() + "_subprop",
                  predicateEntity, RDFS_SUBPROPERTYOF, SDMX_MEASURE_OBSVALUE));
              if ((classificationSet != null) && !classificationSet.isEmpty()) {
                final Entity classificationEntity = classificationSet.iterator().next().getEntity();
                columns.add(createTripleColumn(predicateEntity.getResource() + "_concept",
                    predicateEntity, QB_CONCEPT, classificationEntity));
              }

              // observation relations
              columns.add(createRelationColumn(obsIndex, predicateEntity, i));
              break;
            default:
              break;
          }
        }
      }
    }

    return new AnnotatedTable(new TableContext(this.prefixes), input.identifier(),
        new TableSchema(columns));
  }

  private String typeFormat(final String text) {
    return String.format("%s_type", text);
  }

  private String urlFormat(final String text) {
    return String.format("%s_url", text);
  }
}
