package cz.cuni.mff.xrg.odalic.bases.types;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.bases.ProxyDefinitionFactory;
import cz.cuni.mff.xrg.odalic.bases.TextSearchingMethod;
import cz.cuni.mff.xrg.odalic.groups.Group;
import uk.ac.shef.dcs.kbproxy.sparql.SparqlProxyDefinition;

@Component
public final class SparqlKnowledgeBaseDefinitionFactory
    implements ProxyDefinitionFactory {

  @Override
  public SparqlProxyDefinition create(final KnowledgeBase base, final Set<? extends Group> availableGroups) {
    final SparqlProxyDefinition.Builder builder = SparqlProxyDefinition.builder();
    
    // Name
    builder.setName(base.getQualifiedName());

    // Insert
    builder.setInsertSupported(base.isInsertEnabled());
    
    if (base.isInsertEnabled()) {
      builder.setInsertPrefixData(base.getUserResourcesPrefix());
      builder.setInsertPrefixSchema(base.getUserClassesPrefix());
    }
    
    // Endpoint
    builder.setEndpoint(base.getEndpoint().toString());
    builder.setLogin(base.getLogin());
    builder.setPassword(base.getPassword());

    // Fulltext settings
    builder.setFulltextEnabled(base.getTextSearchingMethod() == TextSearchingMethod.SUBSTRING || base.getTextSearchingMethod() == TextSearchingMethod.FULLTEXT);
    builder.setUseBifContains(base.getTextSearchingMethod() == TextSearchingMethod.FULLTEXT);

    // Language suffix
    builder.setLanguageSuffix(base.getLanguageTag());

    // Use default class type mode
    builder.setClassTypeMode(SparqlProxyDefinition.SEARCH_CLASS_TYPE_MODE_VALUE.INDIRECT);

    // Loading structure
    builder.setGroupsAutoSelected(base.getGroupsAutoSelected());
    final Set<? extends Group> usedGroups = getUsedGroups(base, availableGroups);
    for (final Group group : usedGroups) {
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
      builder.setInsertTypeDataProperty(base.getInsertDataPropertyType().toString());
      builder.setInsertTypeObjectProperty(base.getInsertObjectPropertyType().toString());
    }
    
    builder.setStoppedClasses(base.getSkippedClasses());
    builder.setStoppedAttributes(base.getSkippedAttributes());
    
    // Apply URI heuristics
    builder.setUriLabelHeuristicApplied(true);
      
    return builder.build();
  }

  private Set<Group> getUsedGroups(final KnowledgeBase base, final Set<? extends Group> availableGroups) {
    if (base.getGroupsAutoSelected()) {

      // TODO: Implement groups detection.
      return ImmutableSet.copyOf(availableGroups);
    } else {
      return base.getSelectedGroups(); // If auto-detection turned off, just use the ones manually set.
    }
  }
}
