package cz.cuni.mff.xrg.odalic.outputs.annotatedtable;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.values.ComponentTypeValue;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
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
      final URI kbUri = knowledgeBaseProxyFactory.getKBProxies().get(configuration.getPrimaryBase().getName())
          .getKbDefinition().getInsertSchemaElementPrefix();
      final String datasetUri = String.format("%sdataset_%s", kbUri, input.identifier());
      final String dsdUri = String.format("%sdsd_%s", kbUri, input.identifier());
      
      // dataset definition
      columns.add(createTripleColumn(builder, "dataset_type", datasetUri, RDF_TYPE, QB_DATASET));
      columns.add(createTripleColumn(builder, "dataset_title", datasetUri, DCTERMS_TITLE, input.identifier()));
      columns.add(createTripleColumn(builder, "dataset_structure", datasetUri, QB_STRUCTURE, dsdUri));
      
      // data structure definition
      columns.add(createTripleColumn(builder, "dsd_type", dsdUri, RDF_TYPE, QB_DATASTRUCTUREDEFINITION));
      
      // observation definition
      columns.add(createClassificationColumn(builder, OBSERVATION, QB_OBSERVATION));
      columns.add(createPredColumn(builder, OBSERVATION + "_dataset", QB_DATASET_PRED, OBSERVATION, datasetUri));
      
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
              columns.add(createTripleColumn(builder, "dsd_component", dsdUri, QB_COMPONENT,
                  compUri(kbUri, i, input.identifier())));
              columns.add(createTripleColumn(builder, String.format("comp%s_kind", i + 1),
                  compUri(kbUri, i, input.identifier()), QB_DIMENSION, predicateEntity.getResource()));
              
              // dimension definition
              columns.add(createTripleColumn(builder, predicateEntity.getResource() + "_type",
                  predicateEntity.getResource(), RDF_TYPE, RDF_PROPERTY));
              columns.add(createTripleColumn(builder, predicateEntity.getResource() + "_type",
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
              columns.add(createTripleColumn(builder, "dsd_component", dsdUri, QB_COMPONENT,
                  compUri(kbUri, i, input.identifier())));
              columns.add(createTripleColumn(builder, String.format("comp%s_kind", i + 1),
                  compUri(kbUri, i, input.identifier()), QB_MEASURE, predicateEntity.getResource()));
              
              // measure definition
              columns.add(createTripleColumn(builder, predicateEntity.getResource() + "_type",
                  predicateEntity.getResource(), RDF_TYPE, RDF_PROPERTY));
              columns.add(createTripleColumn(builder, predicateEntity.getResource() + "_type",
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
    
    return new AnnotatedTable(input.identifier(), new TableSchema(columns));
  }
  
  private static final String DCTERMS_TITLE = "dcterms:title";
  private static final String RDF_TYPE = "rdf:type";
  private static final String OWL_SAMEAS = "owl:sameAs";
  
  private static final String RDF_PROPERTY = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Property";
  private static final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
  private static final String RDFS_SUBPROPERTYOF = "http://www.w3.org/2000/01/rdf-schema#subPropertyOf";
  
  private static final String QB_DATASET = "http://purl.org/linked-data/cube#DataSet";
  private static final String QB_STRUCTURE = "http://purl.org/linked-data/cube#structure";
  private static final String QB_DATASTRUCTUREDEFINITION = "http://purl.org/linked-data/cube#DataStructureDefinition";
  private static final String QB_OBSERVATION = "http://purl.org/linked-data/cube#Observation";
  private static final String QB_DATASET_PRED = "http://purl.org/linked-data/cube#dataSet";
  private static final String QB_COMPONENT = "http://purl.org/linked-data/cube#component";
  private static final String QB_DIMENSION = "http://purl.org/linked-data/cube#dimension";
  private static final String QB_MEASURE = "http://purl.org/linked-data/cube#measure";
  private static final String QB_DIMENSIONPROPERTY = "http://purl.org/linked-data/cube#DimensionProperty";
  private static final String QB_MEASUREPROPERTY = "http://purl.org/linked-data/cube#MeasureProperty";
  private static final String QB_CONCEPT = "http://purl.org/linked-data/cube#concept";
  private static final String SDMX_MEASURE_OBSVALUE = "http://purl.org/linked-data/sdmx/2009/measure#obsValue";
  
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
    builder.setPropertyUrl(DCTERMS_TITLE);
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
    builder.setPropertyUrl(RDF_TYPE);
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
    builder.setPropertyUrl(OWL_SAMEAS);
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
  
  private TableColumn createPredColumn(TableColumnBuilder builder, String name, String predicate, String columnName, String resource) {
    builder.clear();
    builder.setName(name);
    builder.setVirtual(true);
    builder.setAboutUrl(bracketFormat(urlFormat(columnName)));
    builder.setPropertyUrl(predicate);
    builder.setValueUrl(resource);
    return builder.build();
  }
  
  private TableColumn createTripleColumn(TableColumnBuilder builder, String name, String subject, String predicate, String object) {
    builder.clear();
    builder.setName(name);
    builder.setVirtual(true);
    builder.setAboutUrl(subject);
    builder.setPropertyUrl(predicate);
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
  
  private String compUri(URI kbUri, int i, String identifier) {
    return String.format("%scomp%s_%s", kbUri, i + 1, identifier);
  }
}
