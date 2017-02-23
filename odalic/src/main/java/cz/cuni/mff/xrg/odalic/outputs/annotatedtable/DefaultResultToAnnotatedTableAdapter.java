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

import cz.cuni.mff.xrg.odalic.api.rest.values.ComponentTypeValue;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.Prefix;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.executions.KnowledgeBaseProxyFactory;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;
import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.KBProxyException;

/**
 * The default {@link ResultToAnnotatedTableAdapter} implementation.
 * 
 * @author Josef Janoušek
 *
 */
public class DefaultResultToAnnotatedTableAdapter implements ResultToAnnotatedTableAdapter {

  private static final Logger log = LoggerFactory.getLogger(DefaultResultToAnnotatedTableAdapter.class);
  
  private final KnowledgeBaseProxyFactory knowledgeBaseProxyFactory;
  
  private TableColumnBuilder builder = new TableColumnBuilder();
  private List<String> headers;
  private boolean[] isDisambiguated;
  private KBProxy kbProxy;
  private Map<String, String> prefixes;
  
  @Autowired
  public DefaultResultToAnnotatedTableAdapter(final KnowledgeBaseProxyFactory knowledgeBaseProxyFactory) {
    Preconditions.checkNotNull(knowledgeBaseProxyFactory);
    
    this.knowledgeBaseProxyFactory = knowledgeBaseProxyFactory;
  }
  
  /**
   * The default toAnnotatedTable implementation.
   * 
   * @see cz.cuni.mff.xrg.odalic.outputs.annotatedtable.ResultToAnnotatedTableAdapter#toAnnotatedTable(cz.cuni.mff.xrg.odalic.results.Result, cz.cuni.mff.xrg.odalic.input.Input, cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration)
   */
  @Override
  public AnnotatedTable toAnnotatedTable(Result result, Input input, Configuration configuration) {
    
    headers = new ArrayList<>(input.headers());
    if (configuration.isStatistical()) {
      headers.add(OBSERVATION);
    }
    
    isDisambiguated = new boolean[headers.size()];
    kbProxy = knowledgeBaseProxyFactory.getKBProxies().get(configuration.getPrimaryBase().getName());
    
    prefixes = new HashMap<>();
    putPrefix(PREFIX_XSD);
    putPrefix(PREFIX_OWL);
    putPrefix(PREFIX_DCTERMS);
    
    List<TableColumn> columns = new ArrayList<TableColumn>();
    
    for (int i = 0; i < input.columnsCount(); i++) {
      boolean addPrimary = false;
      boolean addAlternatives = false;
      
      for (int j = 0; j < input.rowsCount(); j++) {
        for (Entry<KnowledgeBase, Set<EntityCandidate>> entry : result.getCellAnnotations()[j][i].getChosen().entrySet()) {
          if (entry.getValue() != null && !entry.getValue().isEmpty()) {
            if (entry.getKey().getName().equals(configuration.getPrimaryBase().getName())) {
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
      
      isDisambiguated[i] = addPrimary || addAlternatives;
      
      columns.add(createOriginalColumn(i));
      
      if (isDisambiguated[i]) {
        columns.add(createDisambiguationColumn(i));
      }
      
      if (addAlternatives) {
        columns.add(createAlternativeDisambiguationColumn(i));
      }
      
      for (Set<EntityCandidate> set : result.getHeaderAnnotations().get(i).getChosen().values()) {
        if (set != null && !set.isEmpty()) {
          for (EntityCandidate chosen : set) {
            columns.add(createClassificationColumn(i, chosen.getEntity()));
          }
        }
      }
    }
    
    for (Entry<ColumnRelationPosition, ColumnRelationAnnotation> entry : result.getColumnRelationAnnotations().entrySet()) {
      Set<EntityCandidate> chosenRelations = entry.getValue().getChosen().get(configuration.getPrimaryBase());
      
      if (chosenRelations != null) {
        for (EntityCandidate chosen : chosenRelations) {
          columns.add(createRelationColumn(entry.getKey().getFirstIndex(), chosen.getEntity(), entry.getKey().getSecondIndex()));
        }
      }
    }
    
    if (configuration.isStatistical()) {
      int obsIndex = headers.size() - 1;
      isDisambiguated[obsIndex] = true;
      
      final URI kbUri = kbProxy.getKbDefinition().getInsertSchemaElementPrefix();
      final Entity datasetEntity = Entity.of(String.format("%sdataset/%s", kbUri, generateStringUUID()), "");
      final Entity dsdEntity = Entity.of(String.format("%sdsd/%s", kbUri, generateStringUUID()), "");
      
      // dataset definition
      columns.add(createTripleColumn(typeFormat("dataset"), datasetEntity, RDF_TYPE, QB_DATASET));
      columns.add(createTripleColumn("dataset_title", datasetEntity, DCTERMS_TITLE, input.identifier()));
      columns.add(createTripleColumn("dataset_structure", datasetEntity, QB_STRUCTURE, dsdEntity));
      
      // data structure definition
      columns.add(createTripleColumn(typeFormat("dsd"), dsdEntity, RDF_TYPE, QB_DATASTRUCTUREDEFINITION));
      
      // observation definition
      columns.add(createDisambiguationColumn(obsIndex));
      columns.add(createClassificationColumn(obsIndex, QB_OBSERVATION));
      columns.add(createPredColumn(OBSERVATION + "_dataset", obsIndex, QB_DATASET_PRED, datasetEntity));
      
      Entity compEntity;
      for (int i = 0; i < input.columnsCount(); i++) {
        Set<EntityCandidate> predicateSet = result.getStatisticalAnnotations().get(i).getPredicate()
            .get(configuration.getPrimaryBase());
        
        if (predicateSet != null && !predicateSet.isEmpty()) {
          ComponentTypeValue componentType = result.getStatisticalAnnotations().get(i).getComponent()
              .get(configuration.getPrimaryBase());
          
          Entity predicateEntity = predicateSet.iterator().next().getEntity();
          
          Set<EntityCandidate> classificationSet = result.getHeaderAnnotations().get(i).getChosen()
              .get(configuration.getPrimaryBase());
          
          switch (componentType) {
            case DIMENSION:
              // component definition
              compEntity = Entity.of(String.format("%sdimension/%s", kbUri, generateStringUUID()), "");
              columns.add(createTripleColumn("dsd_component", dsdEntity, QB_COMPONENT, compEntity));
              columns.add(createTripleColumn("component_kind", compEntity, QB_DIMENSION, predicateEntity));
              
              // dimension definition
              columns.add(createTripleColumn(typeFormat(predicateEntity.getResource()),
                  predicateEntity, RDF_TYPE, RDF_PROPERTY));
              columns.add(createTripleColumn(typeFormat(predicateEntity.getResource()),
                  predicateEntity, RDF_TYPE, QB_DIMENSIONPROPERTY));
              columns.add(createTripleColumn(predicateEntity.getResource() + "_label",
                  predicateEntity, RDFS_LABEL, predicateEntity.getLabel()));
              if (classificationSet != null && !classificationSet.isEmpty()) {
                Entity classificationEntity = classificationSet.iterator().next().getEntity();
                columns.add(createTripleColumn(predicateEntity.getResource() + "_concept",
                    predicateEntity, QB_CONCEPT, classificationEntity));
              }
              
              // observation relations
              columns.add(createRelationColumn(obsIndex, predicateEntity, i));
              break;
            case MEASURE:
              // component definition
              compEntity = Entity.of(String.format("%smeasure/%s", kbUri, generateStringUUID()), "");
              columns.add(createTripleColumn("dsd_component", dsdEntity, QB_COMPONENT, compEntity));
              columns.add(createTripleColumn("component_kind", compEntity, QB_MEASURE, predicateEntity));
              
              // measure definition
              columns.add(createTripleColumn(typeFormat(predicateEntity.getResource()),
                  predicateEntity, RDF_TYPE, RDF_PROPERTY));
              columns.add(createTripleColumn(typeFormat(predicateEntity.getResource()),
                  predicateEntity, RDF_TYPE, QB_MEASUREPROPERTY));
              columns.add(createTripleColumn(predicateEntity.getResource() + "_label",
                  predicateEntity, RDFS_LABEL, predicateEntity.getLabel()));
              columns.add(createTripleColumn(predicateEntity.getResource() + "_subprop",
                  predicateEntity, RDFS_SUBPROPERTYOF, SDMX_MEASURE_OBSVALUE));
              if (classificationSet != null && !classificationSet.isEmpty()) {
                Entity classificationEntity = classificationSet.iterator().next().getEntity();
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
    
    return new AnnotatedTable(new TableContext(prefixes), input.identifier(), new TableSchema(columns));
  }
  
  private static final Prefix PREFIX_RDF = Prefix.create(RDF.PREFIX, RDF.NAMESPACE);
  private static final Entity RDF_TYPE = Entity.of(PREFIX_RDF, RDF.TYPE.stringValue(), "");
  private static final Entity RDF_PROPERTY = Entity.of(PREFIX_RDF, RDF.PROPERTY.stringValue(), "");
  
  private static final Prefix PREFIX_OWL = Prefix.create(OWL.PREFIX, OWL.NAMESPACE);
  private static final Entity OWL_SAMEAS = Entity.of(PREFIX_OWL, OWL.SAMEAS.stringValue(), "");
  
  private static final Prefix PREFIX_DCTERMS = Prefix.create(DCTERMS.PREFIX, DCTERMS.NAMESPACE);
  private static final Entity DCTERMS_TITLE = Entity.of(PREFIX_DCTERMS, DCTERMS.TITLE.stringValue(), "");
  
  private static final Prefix PREFIX_RDFS = Prefix.create(RDFS.PREFIX, RDFS.NAMESPACE);
  private static final Entity RDFS_LABEL = Entity.of(PREFIX_RDFS, RDFS.LABEL.stringValue(), "");
  private static final Entity RDFS_SUBPROPERTYOF = Entity.of(PREFIX_RDFS, RDFS.SUBPROPERTYOF.stringValue(), "");
  
  private static final Prefix PREFIX_XSD = Prefix.create(XMLSchema.PREFIX, XMLSchema.NAMESPACE);
  private static final Entity XSD_STRING = Entity.of(PREFIX_XSD, XMLSchema.STRING.stringValue(), "");
  private static final Entity XSD_ANYURI = Entity.of(PREFIX_XSD, XMLSchema.ANYURI.stringValue(), "");
  
  private static final String QB = "http://purl.org/linked-data/cube#";
  private static final Prefix PREFIX_QB = Prefix.create("qb", QB);
  private static final Entity QB_DATASET = Entity.of(PREFIX_QB, QB + "DataSet", "");
  private static final Entity QB_STRUCTURE = Entity.of(PREFIX_QB, QB + "structure", "");
  private static final Entity QB_DATASTRUCTUREDEFINITION = Entity.of(PREFIX_QB, QB + "DataStructureDefinition", "");
  private static final Entity QB_OBSERVATION = Entity.of(PREFIX_QB, QB + "Observation", "");
  private static final Entity QB_DATASET_PRED = Entity.of(PREFIX_QB, QB + "dataSet", "");
  private static final Entity QB_COMPONENT = Entity.of(PREFIX_QB, QB + "component", "");
  private static final Entity QB_DIMENSION = Entity.of(PREFIX_QB, QB + "dimension", "");
  private static final Entity QB_MEASURE = Entity.of(PREFIX_QB, QB + "measure", "");
  private static final Entity QB_DIMENSIONPROPERTY = Entity.of(PREFIX_QB, QB + "DimensionProperty", "");
  private static final Entity QB_MEASUREPROPERTY = Entity.of(PREFIX_QB, QB + "MeasureProperty", "");
  private static final Entity QB_CONCEPT = Entity.of(PREFIX_QB, QB + "concept", "");
  
  private static final Prefix PREFIX_SDMX_MEASURE = Prefix.create("sdmx-measure", "http://purl.org/linked-data/sdmx/2009/measure#");
  private static final Entity SDMX_MEASURE_OBSVALUE = Entity.of(PREFIX_SDMX_MEASURE, PREFIX_SDMX_MEASURE.getWhat() + "obsValue", "");
  
  private static final String SEPARATOR = " ";
  private static final String OBSERVATION = "OBSERVATION";
  
  private TableColumn createOriginalColumn(int columnIndex) {
    String columnName = headers.get(columnIndex);
    
    builder.clear();
    builder.setName(columnName);
    builder.setTitles(Arrays.asList(columnName));
    builder.setDataType(XSD_STRING.getPrefixed());
    
    if (isDisambiguated[columnIndex]) {
      builder.setAboutUrl(bracketFormat(urlFormat(columnName)));
      builder.setPropertyUrl(DCTERMS_TITLE.getPrefixed());
    }
    
    return builder.build();
  }
  
  private TableColumn createDisambiguationColumn(int columnIndex) {
    String columnName = headers.get(columnIndex);
    
    builder.clear();
    builder.setName(urlFormat(columnName));
    builder.setDataType(XSD_ANYURI.getPrefixed());
    builder.setSuppressOutput(true);
    builder.setValueUrl(bracketFormat(urlFormat(columnName)));
    
    return builder.build();
  }
  
  private TableColumn createAlternativeDisambiguationColumn(int columnIndex) {
    String columnName = headers.get(columnIndex);
    
    builder.clear();
    builder.setName(alternativeUrlsFormat(columnName));
    builder.setAboutUrl(bracketFormat(urlFormat(columnName)));
    builder.setSeparator(SEPARATOR);
    builder.setPropertyUrl(OWL_SAMEAS.getPrefixed());
    builder.setValueUrl(bracketFormat(alternativeUrlsFormat(columnName)));
    
    return builder.build();
  }
  
  private TableColumn createRelationColumn(int subjectColumnIndex, Entity predicate, int objectColumnIndex) {
    String subjectColumnName = headers.get(subjectColumnIndex);
    String objectColumnName = headers.get(objectColumnIndex);
    
    putPrefix(predicate.getPrefix());
    
    builder.clear();
    builder.setName(predicate.getResource());
    builder.setVirtual(true);
    builder.setAboutUrl(bracketFormat(urlFormat(subjectColumnName)));
    builder.setPropertyUrl(predicate.getPrefixed());
    
    if (isDisambiguated[objectColumnIndex]) {
      builder.setValueUrl(bracketFormat(urlFormat(objectColumnName)));
    }
    else {
      builder.setValueUrl(bracketFormat(objectColumnName));
      List<String> ranges;
      try {
        ranges = kbProxy.getPropertyRanges(predicate.getResource());
      } catch (KBProxyException e) {
        log.warn("Ranges not found for predicate " + predicate.getResource(), e);
        ranges = new ArrayList<>();
      }
      if (!ranges.isEmpty()) {
        Entity dataType;
        if (knowledgeBaseProxyFactory.getPrefixService() != null) {
          dataType = Entity.of(knowledgeBaseProxyFactory.getPrefixService().getPrefix(ranges.get(0)), ranges.get(0), "");
          putPrefix(dataType.getPrefix());
        }
        else {
          dataType = Entity.of(ranges.get(0), "");
        }
        
        builder.setDataType(dataType.getPrefixed());
      }
    }
    
    return builder.build();
  }
  
  private TableColumn createClassificationColumn(int subjectColumnIndex, Entity object) {
    return createPredColumn(typeFormat(headers.get(subjectColumnIndex)), subjectColumnIndex, RDF_TYPE, object);
  }
  
  private TableColumn createPredColumn(String name, int subjectColumnIndex, Entity predicate, Entity object) {
    putPrefix(object.getPrefix());
    
    return createVirtualColumn(name, bracketFormat(urlFormat(headers.get(subjectColumnIndex))), predicate, object.getPrefixed());
  }
  
  private TableColumn createTripleColumn(String name, Entity subject, Entity predicate, Entity object) {
    putPrefix(object.getPrefix());
    
    return createTripleColumn(name, subject, predicate, object.getPrefixed());
  }
  private TableColumn createTripleColumn(String name, Entity subject, Entity predicate, String object) {
    putPrefix(subject.getPrefix());
    
    return createVirtualColumn(name, subject.getPrefixed(), predicate, object);
  }
  
  private TableColumn createVirtualColumn(String name, String subject, Entity predicate, String object) {
    putPrefix(predicate.getPrefix());
    
    builder.clear();
    builder.setName(name);
    builder.setVirtual(true);
    builder.setAboutUrl(subject);
    builder.setPropertyUrl(predicate.getPrefixed());
    builder.setValueUrl(object);
    
    return builder.build();
  }
  
  private void putPrefix(Prefix prefix) {
    if (prefix != null && !prefixes.containsKey(prefix.getWith())) {
      prefixes.put(prefix.getWith(), prefix.getWhat());
    }
  }
  
  private String urlFormat(String text) {
    return String.format("%s_url", text);
  }
  
  private String alternativeUrlsFormat(String text) {
    return String.format("%s_alternative_urls", text);
  }
  
  private String typeFormat(String text) {
    return String.format("%s_type", text);
  }
  
  private String bracketFormat(String text) {
    return String.format("{%s}", text);
  }
  
  private String generateStringUUID() {
    return UUID.randomUUID().toString();
  }
}
