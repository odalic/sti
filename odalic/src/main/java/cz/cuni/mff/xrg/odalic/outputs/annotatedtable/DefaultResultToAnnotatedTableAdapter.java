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

/**
 * The default {@link ResultToAnnotatedTableAdapter} implementation.
 * 
 * @author Josef Janoušek
 *
 */
public class DefaultResultToAnnotatedTableAdapter implements ResultToAnnotatedTableAdapter {

  private final KnowledgeBaseProxyFactory knowledgeBaseProxyFactory;
  
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
    
    Map<String, String> prefixes = new HashMap<>();
    prefixes.put(PREFIX_RDF.getWith(), PREFIX_RDF.getWhat());
    prefixes.put(PREFIX_OWL.getWith(), PREFIX_OWL.getWhat());
    prefixes.put(PREFIX_DCTERMS.getWith(), PREFIX_DCTERMS.getWhat());
    
    TableColumnBuilder builder = new TableColumnBuilder();
    List<TableColumn> columns = new ArrayList<TableColumn>();
    List<String> headers = input.headers();
    
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
      
      if (addPrimary || addAlternatives) {
        columns.add(createOriginalDisambiguatedColumn(builder, headers.get(i)));
        
        columns.add(createDisambiguationColumn(builder, headers.get(i)));
      } else {
        columns.add(createOriginalNonDisambiguatedColumn(builder, headers.get(i)));
      }
      
      if (addAlternatives) {
        columns.add(createAlternativeDisambiguationColumn(builder, headers.get(i)));
      }
      
      for (Set<EntityCandidate> set : result.getHeaderAnnotations().get(i).getChosen().values()) {
        if (set != null && !set.isEmpty()) {
          for (EntityCandidate chosen : set) {
            columns.add(createClassificationColumn(builder, headers.get(i), chosen.getEntity().getResource()));
          }
        }
      }
    }
    
    for (Entry<ColumnRelationPosition, ColumnRelationAnnotation> entry : result.getColumnRelationAnnotations().entrySet()) {
      Set<EntityCandidate> chosenRelations = entry.getValue().getChosen().get(configuration.getPrimaryBase());
      
      if (chosenRelations != null) {
        for (EntityCandidate chosen : chosenRelations) {
          columns.add(createRelationColumn(builder, chosen.getEntity().getResource(),
              headers.get(entry.getKey().getFirstIndex()), headers.get(entry.getKey().getSecondIndex())));
        }
      }
    }
    
    if (configuration.isStatistical()) {
      prefixes.put(PREFIX_RDFS.getWith(), PREFIX_RDFS.getWhat());
      prefixes.put(PREFIX_QB.getWith(), PREFIX_QB.getWhat());
      prefixes.put(PREFIX_SDMX_MEASURE.getWith(), PREFIX_SDMX_MEASURE.getWhat());
      
      final URI kbUri = knowledgeBaseProxyFactory.getKBProxies().get(configuration.getPrimaryBase().getName())
          .getKbDefinition().getInsertSchemaElementPrefix();
      final String datasetUri = String.format("%sdataset/%s", kbUri, generateStringUUID());
      final String dsdUri = String.format("%sdsd/%s", kbUri, generateStringUUID());
      
      // dataset definition
      columns.add(createTripleColumn(builder, typeFormat("dataset"), datasetUri, RDF_TYPE, QB_DATASET));
      columns.add(createTripleColumn(builder, "dataset_title", datasetUri, DCTERMS_TITLE, input.identifier()));
      columns.add(createTripleColumn(builder, "dataset_structure", datasetUri, QB_STRUCTURE, dsdUri));
      
      // data structure definition
      columns.add(createTripleColumn(builder, typeFormat("dsd"), dsdUri, RDF_TYPE, QB_DATASTRUCTUREDEFINITION));
      
      // observation definition
      columns.add(createDisambiguationColumn(builder, OBSERVATION));
      columns.add(createClassificationColumn(builder, OBSERVATION, QB_OBSERVATION.getPrefixed()));
      columns.add(createPredColumn(builder, OBSERVATION + "_dataset", QB_DATASET_PRED, OBSERVATION, datasetUri));
      
      String compUri;
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
              compUri = String.format("%sdimension/%s", kbUri, generateStringUUID());
              columns.add(createTripleColumn(builder, "dsd_component", dsdUri, QB_COMPONENT, compUri));
              columns.add(createTripleColumn(builder, "component_kind", compUri, QB_DIMENSION,
                  predicateEntity.getResource()));
              
              // dimension definition
              columns.add(createTripleColumn(builder, typeFormat(predicateEntity.getResource()),
                  predicateEntity.getResource(), RDF_TYPE, RDF_PROPERTY));
              columns.add(createTripleColumn(builder, typeFormat(predicateEntity.getResource()),
                  predicateEntity.getResource(), RDF_TYPE, QB_DIMENSIONPROPERTY));
              columns.add(createTripleColumn(builder, predicateEntity.getResource() + "_label",
                  predicateEntity.getResource(), RDFS_LABEL, predicateEntity.getLabel()));
              if (classificationSet != null && !classificationSet.isEmpty()) {
                Entity classificationEntity = classificationSet.iterator().next().getEntity();
                columns.add(createTripleColumn(builder, predicateEntity.getResource() + "_concept",
                    predicateEntity.getResource(), QB_CONCEPT, classificationEntity.getResource()));
              }
              
              // observation relations
              columns.add(createRelationColumn(builder, predicateEntity.getResource(), OBSERVATION, headers.get(i)));
              break;
            case MEASURE:
              // component definition
              compUri = String.format("%smeasure/%s", kbUri, generateStringUUID());
              columns.add(createTripleColumn(builder, "dsd_component", dsdUri, QB_COMPONENT, compUri));
              columns.add(createTripleColumn(builder, "component_kind", compUri, QB_MEASURE,
                  predicateEntity.getResource()));
              
              // measure definition
              columns.add(createTripleColumn(builder, typeFormat(predicateEntity.getResource()),
                  predicateEntity.getResource(), RDF_TYPE, RDF_PROPERTY));
              columns.add(createTripleColumn(builder, typeFormat(predicateEntity.getResource()),
                  predicateEntity.getResource(), RDF_TYPE, QB_MEASUREPROPERTY));
              columns.add(createTripleColumn(builder, predicateEntity.getResource() + "_label",
                  predicateEntity.getResource(), RDFS_LABEL, predicateEntity.getLabel()));
              columns.add(createTripleColumn(builder, predicateEntity.getResource() + "_subprop",
                  predicateEntity.getResource(), RDFS_SUBPROPERTYOF, SDMX_MEASURE_OBSVALUE));
              if (classificationSet != null && !classificationSet.isEmpty()) {
                Entity classificationEntity = classificationSet.iterator().next().getEntity();
                columns.add(createTripleColumn(builder, predicateEntity.getResource() + "_concept",
                    predicateEntity.getResource(), QB_CONCEPT, classificationEntity.getResource()));
              }
              
              // observation relations (measures)
              columns.add(createMeasureColumn(builder, predicateEntity.getResource(), OBSERVATION, headers.get(i)));
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
  private static final String STRING = "string";
  private static final String ANY_URI = "anyURI";
  private static final String OBSERVATION = "OBSERVATION";
  
  private TableColumn createOriginalDisambiguatedColumn(TableColumnBuilder builder, String columnName) {
    builder.clear();
    builder.setName(columnName);
    builder.setTitles(Arrays.asList(columnName));
    builder.setDataType(STRING);
    builder.setAboutUrl(bracketFormat(urlFormat(columnName)));
    builder.setPropertyUrl(DCTERMS_TITLE.getPrefixed());
    return builder.build();
  }
  
  private TableColumn createOriginalNonDisambiguatedColumn(TableColumnBuilder builder, String columnName) {
    builder.clear();
    builder.setName(columnName);
    builder.setTitles(Arrays.asList(columnName));
    builder.setDataType(STRING);
    return builder.build();
  }
  
  private TableColumn createClassificationColumn(TableColumnBuilder builder, String columnName, String resource) {
    builder.clear();
    builder.setName(typeFormat(columnName));
    builder.setVirtual(true);
    builder.setAboutUrl(bracketFormat(urlFormat(columnName)));
    builder.setPropertyUrl(RDF_TYPE.getPrefixed());
    builder.setValueUrl(resource);
    return builder.build();
  }
  
  private TableColumn createDisambiguationColumn(TableColumnBuilder builder, String columnName) {
    builder.clear();
    builder.setName(urlFormat(columnName));
    builder.setDataType(ANY_URI);
    builder.setSuppressOutput(true);
    builder.setValueUrl(bracketFormat(urlFormat(columnName)));
    return builder.build();
  }
  
  private TableColumn createAlternativeDisambiguationColumn(TableColumnBuilder builder, String columnName) {
    builder.clear();
    builder.setName(alternativeUrlsFormat(columnName));
    builder.setAboutUrl(bracketFormat(urlFormat(columnName)));
    builder.setSeparator(SEPARATOR);
    builder.setPropertyUrl(OWL_SAMEAS.getPrefixed());
    builder.setValueUrl(bracketFormat(alternativeUrlsFormat(columnName)));
    return builder.build();
  }
  
  private TableColumn createRelationColumn(TableColumnBuilder builder, String predicateName, String subjectName, String objectName) {
    builder.clear();
    builder.setName(predicateName);
    builder.setVirtual(true);
    builder.setAboutUrl(bracketFormat(urlFormat(subjectName)));
    builder.setPropertyUrl(predicateName);
    builder.setValueUrl(bracketFormat(urlFormat(objectName)));
    return builder.build();
  }
  
  private TableColumn createMeasureColumn(TableColumnBuilder builder, String predicateName, String subjectName, String objectName) {
    builder.clear();
    builder.setName(predicateName);
    builder.setVirtual(true);
    builder.setAboutUrl(bracketFormat(urlFormat(subjectName)));
    builder.setPropertyUrl(predicateName);
    builder.setValueUrl(bracketFormat(objectName));
    return builder.build();
  }
  
  private TableColumn createPredColumn(TableColumnBuilder builder, String name, Entity predicate, String columnName, String resource) {
    builder.clear();
    builder.setName(name);
    builder.setVirtual(true);
    builder.setAboutUrl(bracketFormat(urlFormat(columnName)));
    builder.setPropertyUrl(predicate.getPrefixed());
    builder.setValueUrl(resource);
    return builder.build();
  }
  
  private TableColumn createTripleColumn(TableColumnBuilder builder, String name, String subject, Entity predicate, Entity object) {
    return createTripleColumn(builder, name, subject, predicate, object.getPrefixed());
  }
  private TableColumn createTripleColumn(TableColumnBuilder builder, String name, String subject, Entity predicate, String object) {
    builder.clear();
    builder.setName(name);
    builder.setVirtual(true);
    builder.setAboutUrl(subject);
    builder.setPropertyUrl(predicate.getPrefixed());
    builder.setValueUrl(object);
    return builder.build();
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
