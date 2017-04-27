package cz.cuni.mff.xrg.odalic.bases.types;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBaseDefinitionFactory;
import cz.cuni.mff.xrg.odalic.bases.TextSearchingMethod;
import cz.cuni.mff.xrg.odalic.groups.Group;
import uk.ac.shef.dcs.kbproxy.sparql.SparqlBaseProxyDefinition;

public final class SparqlKnowledgeBaseDefinitionFactory
    implements KnowledgeBaseDefinitionFactory<SparqlBaseProxyDefinition> {

  @Override
  public SparqlBaseProxyDefinition create(final KnowledgeBase base, final Class<? extends SparqlBaseProxyDefinition> type) {
    final SparqlBaseProxyDefinition.Builder builder = SparqlBaseProxyDefinition.builder();
    
    // Name
    builder.setName(base.getName());

    // SPARQL insert
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
    builder.setClassTypeMode(SparqlBaseProxyDefinition.SEARCH_CLASS_TYPE_MODE_VALUE.INDIRECT);

    // Loading structure
    for (final Group group : base.getSelectedGroups()) {
        builder.addAllStructurePredicateLabel(group.getLabelPredicates());
        builder.addAllStructurePredicateDescription(group.getDescriptionPredicates());
        builder.addAllStructureTypeClass(group.getClassTypes());
        builder.addAllStructureTypeProperty(group.getPropertyTypes());
        builder.addAllStructurePredicateType(group.getInstanceOfPredicates());
    }

    builder.setStructureInstanceOf(SparqlBaseProxyDefinition.DEFAULT_STRUCTURE_PREDICATE_INSTANCE_OF);
    builder.setStructureDomain(SparqlBaseProxyDefinition.DEFAULT_STRUCTURE_PREDICATE_DOMAIN);
    builder.setStructureRange(SparqlBaseProxyDefinition.DEFAULT_STRUCTURE_PREDICATE_RANGE);

    // SPARQL insert
    if (base.isInsertEnabled()) {
      builder.setInsertGraph(base.getInsertGraph().toString());

      builder.setInsertDefaultClass(SparqlBaseProxyDefinition.DEFAULT_INSERT_DEFAULT_CLASS);
      builder.setInsertPredicateLabel(SparqlBaseProxyDefinition.DEFAULT_INSERT_PREDICATE_LABEL);
      builder.setInsertPredicateAlternativeLabel(SparqlBaseProxyDefinition.DEFAULT_INSERT_PREDICATE_ALTERNATIVE_LABEL);
      builder.setInsertPredicateSubclassOf(SparqlBaseProxyDefinition.DEFAULT_INSERT_PREDICATE_SUBCLASS_OF);
      builder.setInsertPredicateSubPropertyOf(SparqlBaseProxyDefinition.DEFAULT_INSERT_PREDICATE_SUB_PROPERTY_OF);
      builder.setInsertTypeClass(SparqlBaseProxyDefinition.DEFAULT_INSERT_TYPE_CLASS);
      builder.setInsertTypeProperty(SparqlBaseProxyDefinition.DEFAULT_INSERT_TYPE_PROPERTY);
    }
    
    builder.setStoppedClasses(base.getSkippedClasses());
    builder.setStoppedAttributes(base.getSkippedAttributes());
    
    // Apply URI heuristics
    builder.setUriLabelHeuristicApplied(true);
      
    return builder.build();
  }

}
