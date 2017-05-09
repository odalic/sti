package cz.cuni.mff.xrg.odalic.bases.types;

import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.bases.ProxyDefinitionFactory;
import cz.cuni.mff.xrg.odalic.bases.TextSearchingMethod;
import cz.cuni.mff.xrg.odalic.groups.Group;
import uk.ac.shef.dcs.kbproxy.sparql.SparqlProxyDefinition;

@Component
public final class SparqlKnowledgeBaseDefinitionFactory
    implements ProxyDefinitionFactory {

  @Override
  public SparqlProxyDefinition create(final KnowledgeBase base) {
    final SparqlProxyDefinition.Builder builder = SparqlProxyDefinition.builder();
    
    // Name
    builder.setName(base.getQualifiedName());

    // Insert
    builder.setInsertSupported(base.isInsertEnabled());
    
    if (base.isInsertEnabled()) {
      builder.setInsertPrefixData(base.getUserResourcesPrefix());
      builder.setInsertPrefixSchema(base.getUserClassesPrefix());
      builder.setInsertGraph(base.getInsertGraph().toString());
    }
    
    // Endpoint
    builder.setEndpoint(base.getEndpoint().toString());

    // Fulltext settings
    builder.setFulltextEnabled(base.getTextSearchingMethod() == TextSearchingMethod.SUBSTRING || base.getTextSearchingMethod() == TextSearchingMethod.FULLTEXT);
    builder.setUseBifContains(base.getTextSearchingMethod() == TextSearchingMethod.FULLTEXT);

    // Language suffix
    builder.setLanguageSuffix(base.getLanguageTag());

    // Use default class type mode
    builder.setClassTypeMode(SparqlProxyDefinition.SEARCH_CLASS_TYPE_MODE_VALUE.INDIRECT);

    // Loading structure
    for (final Group group : base.getSelectedGroups()) {
        builder.addAllStructurePredicateLabel(group.getLabelPredicates());
        builder.addAllStructurePredicateDescription(group.getDescriptionPredicates());
        builder.addAllStructureTypeClass(group.getClassTypes());
        builder.addAllStructureTypeProperty(group.getPropertyTypes());
        builder.addAllStructurePredicateType(group.getInstanceOfPredicates());
    }

    builder.setStructureInstanceOf(SparqlProxyDefinition.DEFAULT_STRUCTURE_PREDICATE_INSTANCE_OF);
    builder.setStructureDomain(SparqlProxyDefinition.DEFAULT_STRUCTURE_PREDICATE_DOMAIN);
    builder.setStructureRange(SparqlProxyDefinition.DEFAULT_STRUCTURE_PREDICATE_RANGE);

    // SPARQL insert
    if (base.isInsertEnabled()) {
      builder.setInsertGraph(base.getInsertGraph().toString());

      builder.setInsertDefaultClass(SparqlProxyDefinition.DEFAULT_INSERT_DEFAULT_CLASS);
      builder.setInsertPredicateLabel(SparqlProxyDefinition.DEFAULT_INSERT_PREDICATE_LABEL);
      builder.setInsertPredicateAlternativeLabel(SparqlProxyDefinition.DEFAULT_INSERT_PREDICATE_ALTERNATIVE_LABEL);
      builder.setInsertPredicateSubclassOf(SparqlProxyDefinition.DEFAULT_INSERT_PREDICATE_SUBCLASS_OF);
      builder.setInsertPredicateSubPropertyOf(SparqlProxyDefinition.DEFAULT_INSERT_PREDICATE_SUB_PROPERTY_OF);
      builder.setInsertTypeClass(SparqlProxyDefinition.DEFAULT_INSERT_TYPE_CLASS);
      builder.setInsertTypeProperty(SparqlProxyDefinition.DEFAULT_INSERT_TYPE_PROPERTY);
    }
    
    builder.setStoppedClasses(base.getSkippedClasses());
    builder.setStoppedAttributes(base.getSkippedAttributes());
    
    // Apply URI heuristics
    builder.setUriLabelHeuristicApplied(true);
      
    return builder.build();
  }
}
