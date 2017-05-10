package uk.ac.shef.dcs.kbproxy.sparql;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import uk.ac.shef.dcs.kbproxy.ProxyDefinition;

public class SparqlProxyDefinition implements ProxyDefinition {
  
  public static class Builder {
    
    private String name;  
    
    private boolean insertSupported;
    private URI insertPrefixData;
    private URI insertPrefixSchema;
    
    private String endpoint;
    
    private Set<String> structurePredicateLabel = new HashSet<>();
    private Set<String> structurePredicateDescription = new HashSet<>();
    private Set<String> structurePredicateType = new HashSet<>();
    private Set<String> structureTypeClass = new HashSet<>();
    private Set<String> structureTypeProperty = new HashSet<>();
    
    private boolean fulltextEnabled;
    private boolean useBifContains;
    private String languageSuffix;
    
    private SEARCH_CLASS_TYPE_MODE_VALUE classTypeMode;
    
    private String structureInstanceOf;
    private String structureDomain;
    private String structureRange;

    private String insertGraph;

    private String insertDefaultClass;
    private String insertPredicateLabel;
    private String insertPredicateAlternativeLabel;
    private String insertPredicateSubclassOf;
    private String insertPredicateSubPropertyOf;
    private String insertTypeClass;
    private String insertTypeProperty;

    private Set<String> stoppedClasses;
    private Set<String> stoppedAttributes;
    
    public boolean uriLabelHeuristicApplied;


    /**
     * @param name the name to set
     */
    public Builder setName(String name) {
      this.name = name;
      
      return this;
    }
    
    /**
     * @param insertSupported the insertSupported to set
     */
    public Builder setInsertSupported(boolean insertSupported) {
      this.insertSupported = insertSupported;

      return this;
    }

    /**
     * @param insertPrefixData the insertPrefixData to set
     */
    public Builder setInsertPrefixData(URI insertPrefixData) {
      this.insertPrefixData = insertPrefixData;

      return this;
    }
    
    /**
     * @param insertPrefixSchema the insertPrefixSchema to set
     */
    public Builder setInsertPrefixSchema(URI insertPrefixSchema) {
      this.insertPrefixSchema = insertPrefixSchema;

      return this;
    }
    
    public SparqlProxyDefinition build() {
      return new SparqlProxyDefinition(this);
    }

    public Builder setFulltextEnabled(boolean fulltextEnabled) {
      this.fulltextEnabled = fulltextEnabled;

      return this;
    }

    public Builder setInsertPredicateAlternativeLabel(final String insertPredicateAlternativeLabel) {
      this.insertPredicateAlternativeLabel = insertPredicateAlternativeLabel;

      return this;
    }

    public Builder setInsertTypeClass(final String insertTypeClass) {
      this.insertTypeClass = insertTypeClass;

      return this;
    }

    public Builder setInsertGraph(final String insertGraph) {
      this.insertGraph = insertGraph;

      return this;
    }

    public Builder setInsertPredicateLabel(final String insertPredicateLabel) {
      this.insertPredicateLabel = insertPredicateLabel;

      return this;
    }

    public Builder setInsertTypeProperty(final String insertTypeProperty) {
      this.insertTypeProperty = insertTypeProperty;

      return this;
    }

    public Builder setInsertDefaultClass(final String insertDefaultClass) {
      this.insertDefaultClass = insertDefaultClass;

      return this;
    }

    public Builder setInsertPredicateSubclassOf(final String insertPredicateSubclassOf) {
      this.insertPredicateSubclassOf = insertPredicateSubclassOf;

      return this;
    }

    public Builder setInsertPredicateSubPropertyOf(final String insertPredicateSubPropertyOf) {
      this.insertPredicateSubPropertyOf = insertPredicateSubPropertyOf;

      return this;
    }

    public Builder setLanguageSuffix(final String languageSuffix) {
      this.languageSuffix = languageSuffix;

      return this;
    }

    public Builder setClassTypeMode(final SparqlProxyDefinition.SEARCH_CLASS_TYPE_MODE_VALUE classTypeMode) {
      this.classTypeMode = classTypeMode;

      return this;
    }

    public Builder setEndpoint(final String endpoint) {
      this.endpoint = endpoint;

      return this;
    }

    /**
     * @param structurePredicateLabel the structurePredicateLabel to set
     */
    public Builder setStructurePredicateLabel(Set<String> structurePredicateLabel) {
      this.structurePredicateLabel = structurePredicateLabel;

      return this;
    }
    
    public Builder addAllStructurePredicateLabel(Collection<String> structurePredicateLabel) {
      Preconditions.checkNotNull(structurePredicateLabel);
      
      this.structurePredicateLabel.addAll(structurePredicateLabel);

      return this;
    }
    
    public Builder addStructurePredicateLabel(String value) {
      Preconditions.checkNotNull(value);
      
      this.structurePredicateLabel.add(value);

      return this;
    }

    /**
     * @param structurePredicateDescription the structurePredicateDescription to set
     */
    public Builder setStructurePredicateDescription(Set<String> structurePredicateDescription) {
      Preconditions.checkNotNull(structurePredicateDescription);
      
      this.structurePredicateDescription = new HashSet<>(structurePredicateDescription);

      return this;
    }
    
    public Builder addAllStructurePredicateDescription(Collection<String> structurePredicateDescription) {
      Preconditions.checkNotNull(structurePredicateDescription);
      
      this.structurePredicateDescription.addAll(structurePredicateDescription);

      return this;
    }
    
    public Builder addStructurePredicateDescription(String value) {
      Preconditions.checkNotNull(value);
      
      this.structurePredicateDescription.add(value);

      return this;
    }

    /**
     * @param structurePredicateType the structurePredicateType to set
     */
    public Builder setStructurePredicateType(Set<String> structurePredicateType) {
      Preconditions.checkNotNull(structurePredicateType);
      
      this.structurePredicateType = new HashSet<>(structurePredicateType);

      return this;
    }
    
    public Builder addAllStructurePredicateType(Collection<String> structurePredicateType) {
      Preconditions.checkNotNull(structurePredicateType);
      
      this.structurePredicateType.addAll(structurePredicateType);

      return this;
    }
    
    public Builder addStructurePredicateType(String value) {
      this.structurePredicateType.add(value);

      return this;
    }

    /**
     * @param structureTypeClass the structureTypeClass to set
     */
    public Builder setStructureTypeClass(Set<String> structureTypeClass) {
      Preconditions.checkNotNull(structureTypeClass);
      
      this.structureTypeClass = new HashSet<>(structureTypeClass);
      
      return this;
    }
    
    public Builder addAllStructureTypeClass(Collection<String> structureTypeClass) {
      Preconditions.checkNotNull(structureTypeClass);
      
      this.structureTypeClass.addAll(structureTypeClass);

      return this;
    }
    
    public Builder addStructureTypeClass(String value) {
      this.structureTypeClass.add(value);

      return this;
    }

    /**
     * @param structureTypeProperty the structureTypeProperty to set
     */
    public Builder setStructureTypeProperty(Set<String> structureTypeProperty) {
      Preconditions.checkNotNull(structureTypeProperty);
      
      this.structureTypeProperty = new HashSet<>(structureTypeProperty);

      return this;
    }
    
    public Builder addAllStructureTypeProperty(Collection<String> structureTypeProperty) {
      Preconditions.checkNotNull(structureTypeProperty);
      
      this.structureTypeProperty.addAll(structureTypeProperty);

      return this;
    }
    
    public Builder addStructureTypeProperty(String value) {
      this.structureTypeProperty.add(value);

      return this;
    }

    public Builder setStructureDomain(final String structureDomain) {
      this.structureDomain = structureDomain;

      return this;
    }

    public Builder setStructureInstanceOf(final String structureInstanceOf) {
      this.structureInstanceOf = structureInstanceOf;

      return this;
    }

    public Builder setStructureRange(final String structureRange) {
      this.structureRange = structureRange;

      return this;
    }

    public Builder setUseBifContains(final boolean useBifContains) {
      this.useBifContains = useBifContains;
      
      return this;
    }
    
    public Builder setUriLabelHeuristicApplied(final boolean uriLabelHeuristicApplied) {
      this.uriLabelHeuristicApplied = uriLabelHeuristicApplied;
      
      return this;
    }

    public Builder setStoppedClasses(List<String> stoppedClasses) {
      Preconditions.checkNotNull(stoppedClasses);
      
      this.stoppedClasses = new HashSet<>(stoppedClasses);
      
      return this;
    }
    
    public Builder addStoppedClass(String stoppedClass) {
      Preconditions.checkNotNull(stoppedClass);
      
      this.stoppedClasses.add(stoppedClass);
      
      return this;
    }

    public Builder setStoppedAttributes(List<String> stoppedAttributes) {
      Preconditions.checkNotNull(stoppedAttributes);
      
      this.stoppedAttributes = new HashSet<>(stoppedAttributes);
      
      return this;
    }
    
    public Builder addStoppedAttributes(String stoppedAttribute) {
      Preconditions.checkNotNull(stoppedAttribute);
      
      this.stoppedAttributes.add(stoppedAttribute);
      
      return this;
    }
  }
  
  public static enum SEARCH_CLASS_TYPE_MODE_VALUE {
    DIRECT, INDIRECT
  }
  
  public static final String STRUCTURE_PREDICATE_INSTANCE_OF = "kb.structure.predicate.instanceOf";
  public static final String DEFAULT_STRUCTURE_PREDICATE_INSTANCE_OF =
      "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
  
  public static final String STRUCTURE_PREDICATE_DOMAIN = "kb.structure.predicate.domain";
  public static final String DEFAULT_STRUCTURE_PREDICATE_DOMAIN =
      "http://www.w3.org/2000/01/rdf-schema#domain";
  
  public static final String STRUCTURE_PREDICATE_RANGE = "kb.structure.predicate.range";
  public static final String DEFAULT_STRUCTURE_PREDICATE_RANGE =
        "http://www.w3.org/2000/01/rdf-schema#range";
  
  public static final String INSERT_DEFAULT_CLASS = "kb.insert.defaultClass";
  public static final String DEFAULT_INSERT_DEFAULT_CLASS = "http://www.w3.org/2002/07/owl#Thing";
  
  public static final String INSERT_PREDICATE_LABEL = "kb.insert.predicate.label";
  public static final String DEFAULT_INSERT_PREDICATE_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
  
  public static final String INSERT_PREDICATE_ALTERNATIVE_LABEL = "kb.insert.predicate.alternativeLabel";
  public static final String DEFAULT_INSERT_PREDICATE_ALTERNATIVE_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
  
  public static final String INSERT_PREDICATE_SUBCLASS_OF = "kb.insert.predicate.subclassOf";
  public static final String DEFAULT_INSERT_PREDICATE_SUBCLASS_OF = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
  
  public static final String INSERT_PREDICATE_SUB_PROPERTY_OF = "kb.insert.predicate.subPropertyOf";
  public static final String DEFAULT_INSERT_PREDICATE_SUB_PROPERTY_OF = "http://www.w3.org/2000/01/rdf-schema#subPropertyOf";
  
  public static final String INSERT_TYPE_CLASS = "kb.insert.type.class";
  public static final String DEFAULT_INSERT_TYPE_CLASS = "http://www.w3.org/2002/07/owl#Class";
  
  public static final String INSERT_TYPE_PROPERTY = "kb.insert.type.property";
  public static final String DEFAULT_INSERT_TYPE_PROPERTY = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Property";  
  
  public static Builder builder() {
    return new Builder();
  }

  private final String name;
  
  private final boolean insertSupported;
  private final URI insertPrefixData;
  private final URI insertPrefixSchema;
  
  private final String endpoint;
  
  private final Set<String> structurePredicateLabel;
  private final Set<String> structurePredicateDescription;
  private final Set<String> structurePredicateType;
  private final Set<String> structureTypeClass;
  private final Set<String> structureTypeProperty;
  
  private final boolean fulltextEnabled;
  private final boolean useBifContains;
  private final String languageSuffix;
  
  private final SEARCH_CLASS_TYPE_MODE_VALUE classTypeMode;
  
  private final String structureInstanceOf;
  private final String structureDomain;
  private final String structureRange;

  private final String insertGraph;

  private final String insertDefaultClass;
  private final String insertPredicateLabel;
  private final String insertPredicateAlternativeLabel;
  private final String insertPredicateSubclassOf;
  private final String insertPredicateSubPropertyOf;
  private final String insertTypeClass;
  private final String insertTypeProperty;
  
  private final Set<String> stoppedClasses;
  private final Set<String> stoppedAttributes;
  
  private final boolean uriLabelHeuristicApplied;
  
  private SparqlProxyDefinition(final Builder builder) {
    Preconditions.checkArgument(!builder.structurePredicateLabel.isEmpty(), "No label predicate defined!");
    Preconditions.checkArgument(!builder.structurePredicateType.isEmpty(), "No type predicate defined!");
    
    this.name = builder.name;
    this.insertSupported = builder.insertSupported;
    this.insertPrefixData = builder.insertPrefixData;
    this.insertPrefixSchema = builder.insertPrefixSchema;
    
    this.endpoint = builder.endpoint;
    this.structurePredicateLabel = ImmutableSet.copyOf(builder.structurePredicateLabel);
    this.structurePredicateDescription = ImmutableSet.copyOf(builder.structurePredicateDescription);
    this.structurePredicateType = ImmutableSet.copyOf(builder.structurePredicateType);
    this.structureTypeClass = ImmutableSet.copyOf(builder.structureTypeClass);
    this.structureTypeProperty = ImmutableSet.copyOf(builder.structureTypeProperty);
    this.fulltextEnabled = builder.fulltextEnabled;
    this.useBifContains = builder.useBifContains;
    this.languageSuffix = builder.languageSuffix;
    this.classTypeMode = builder.classTypeMode;
    this.structureInstanceOf = builder.structureInstanceOf;
    this.structureDomain = builder.structureDomain;
    this.structureRange = builder.structureRange;
    this.insertGraph = builder.insertGraph;
    this.insertDefaultClass = builder.insertDefaultClass;
    this.insertPredicateLabel = builder.insertPredicateLabel;
    this.insertPredicateAlternativeLabel = builder.insertPredicateAlternativeLabel;
    this.insertPredicateSubclassOf = builder.insertPredicateSubclassOf;
    this.insertPredicateSubPropertyOf = builder.insertPredicateSubPropertyOf;
    this.insertTypeClass = builder.insertTypeClass;
    this.insertTypeProperty = builder.insertTypeProperty;
    this.stoppedClasses = ImmutableSet.copyOf(builder.stoppedClasses);
    this.stoppedAttributes = ImmutableSet.copyOf(builder.stoppedAttributes);
    this.uriLabelHeuristicApplied = builder.uriLabelHeuristicApplied;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }
  
  /**
   * @return the insertSupported
   */
  public boolean isInsertSupported() {
    return insertSupported;
  }
  
  /**
   * @return the insertPrefixData
   */
  public URI getInsertPrefixData() {
    return insertPrefixData;
  }
  
  /**
   * @return the insertPrefixSchema
   */
  public URI getInsertPrefixSchema() {
    return insertPrefixSchema;
  }
  
  /**
   * @return the endpoint
   */
  public String getEndpoint() {
    return endpoint;
  }

  /**
   * @return the structurePredicateLabel
   */
  public Set<String> getStructurePredicateLabel() {
    return structurePredicateLabel;
  }

  /**
   * @return the structurePredicateDescription
   */
  public Set<String> getStructurePredicateDescription() {
    return structurePredicateDescription;
  }

  /**
   * @return the structurePredicateType
   */
  public Set<String> getStructurePredicateType() {
    return structurePredicateType;
  }

  /**
   * @return the structureTypeClass
   */
  public Set<String> getStructureTypeClass() {
    return structureTypeClass;
  }

  /**
   * @return the structureTypeProperty
   */
  public Set<String> getStructureTypeProperty() {
    return structureTypeProperty;
  }

  /**
   * @return the fulltextEnabled
   */
  public boolean isFulltextEnabled() {
    return fulltextEnabled;
  }

  /**
   * @return the useBifContains
   */
  public boolean isUseBifContains() {
    return useBifContains;
  }

  /**
   * @return the languageSuffix
   */
  public String getLanguageSuffix() {
    return languageSuffix;
  }

  /**
   * @return the classTypeMode
   */
  public SparqlProxyDefinition.SEARCH_CLASS_TYPE_MODE_VALUE getClassTypeMode() {
    return classTypeMode;
  }

  /**
   * @return the structureInstanceOf
   */
  public String getStructureInstanceOf() {
    return structureInstanceOf;
  }

  /**
   * @return the structureDomain
   */
  public String getStructureDomain() {
    return structureDomain;
  }

  /**
   * @return the structureRange
   */
  public String getStructureRange() {
    return structureRange;
  }

  /**
   * @return the insertGraph
   */
  public String getInsertGraph() {
    return insertGraph;
  }

  /**
   * @return the insertDefaultClass
   */
  public String getInsertDefaultClass() {
    return insertDefaultClass;
  }

  /**
   * @return the insertPredicateLabel
   */
  public String getInsertPredicateLabel() {
    return insertPredicateLabel;
  }

  /**
   * @return the insertPredicateAlternativeLabel
   */
  public String getInsertPredicateAlternativeLabel() {
    return insertPredicateAlternativeLabel;
  }

  /**
   * @return the insertPredicateSubclassOf
   */
  public String getInsertPredicateSubclassOf() {
    return insertPredicateSubclassOf;
  }

  /**
   * @return the insertPredicateSubPropertyOf
   */
  public String getInsertPredicateSubPropertyOf() {
    return insertPredicateSubPropertyOf;
  }

  /**
   * @return the insertTypeClass
   */
  public String getInsertTypeClass() {
    return insertTypeClass;
  }

  /**
   * @return the insertTypeProperty
   */
  public String getInsertTypeProperty() {
    return insertTypeProperty;
  }

  /**
   * @return the stoppedClasses
   */
  public Set<String> getStoppedClasses() {
    return stoppedClasses;
  }

  /**
   * @return the stoppedAttributes
   */
  public Set<String> getStoppedAttributes() {
    return stoppedAttributes;
  }

  /**
   * @return the uriLabelHeuristicApplied
   */
  public boolean isUriLabelHeuristicApplied() {
    return uriLabelHeuristicApplied;
  }
}
