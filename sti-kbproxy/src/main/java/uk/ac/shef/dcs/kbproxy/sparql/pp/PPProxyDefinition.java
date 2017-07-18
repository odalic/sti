package uk.ac.shef.dcs.kbproxy.sparql.pp;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import uk.ac.shef.dcs.kbproxy.ProxyDefinition;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PPProxyDefinition implements ProxyDefinition {

  public static class Builder {

    private String name;

    private boolean insertSupported;
    //private String insertEndpoint;
    private URI insertPrefixData;
    private URI insertPrefixSchema;

    private String endpoint;
    private String login;
    private String password;

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

    //private String insertGraph;

    private String insertDefaultClass;
    private String insertPredicateLabel;
    private String insertPredicateAlternativeLabel;
    private String insertPredicateSubclassOf;
    private String insertPredicateSubPropertyOf;
    private String insertTypeClass;
    private String insertTypeObjectProperty;
    private String insertTypeDataProperty;

    private Set<String> stoppedClasses;
    private Set<String> stoppedAttributes;

    public boolean uriLabelHeuristicApplied;
    private boolean groupsAutoSelected;


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

//    /**
//     * @param insertEndpoint the insert endpoint
//     */
//    public Builder setInsertEndpoint(final String insertEndpoint) {
//      this.insertEndpoint = insertEndpoint;
//
//      return this;
//    }

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

    public PPProxyDefinition build() {
      return new PPProxyDefinition(this);
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

//    public Builder setInsertGraph(final String insertGraph) {
//      this.insertGraph = insertGraph;
//
//      return this;
//    }

    public Builder setInsertPredicateLabel(final String insertPredicateLabel) {
      this.insertPredicateLabel = insertPredicateLabel;

      return this;
    }

    public Builder setInsertTypeObjectProperty(final String insertTypeObjectProperty) {
      this.insertTypeObjectProperty = insertTypeObjectProperty;

      return this;
    }

    public Builder setInsertTypeDataProperty(final String insertTypeDataProperty) {
      this.insertTypeDataProperty = insertTypeDataProperty;

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

    public Builder setClassTypeMode(final PPProxyDefinition.SEARCH_CLASS_TYPE_MODE_VALUE classTypeMode) {
      this.classTypeMode = classTypeMode;

      return this;
    }

    public Builder setEndpoint(final String endpoint) {
      this.endpoint = endpoint;

      return this;
    }

    public Builder setLogin(final String login) {
      this.login = login;

      return this;
    }

    public Builder setPassword(final String password) {
      this.password = password;

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
      Preconditions.checkNotNull(structurePredicateLabel, "The structurePredicateLabel cannot be null!");

      this.structurePredicateLabel.addAll(structurePredicateLabel);

      return this;
    }

    public Builder addStructurePredicateLabel(String value) {
      Preconditions.checkNotNull(value, "The value cannot be null!");

      this.structurePredicateLabel.add(value);

      return this;
    }

    /**
     * @param structurePredicateDescription the structurePredicateDescription to set
     */
    public Builder setStructurePredicateDescription(Set<String> structurePredicateDescription) {
      Preconditions.checkNotNull(structurePredicateDescription, "The structurePredicateDescription cannot be null!");

      this.structurePredicateDescription = new HashSet<>(structurePredicateDescription);

      return this;
    }

    public Builder addAllStructurePredicateDescription(Collection<String> structurePredicateDescription) {
      Preconditions.checkNotNull(structurePredicateDescription, "The structurePredicateDescription cannot be null!");

      this.structurePredicateDescription.addAll(structurePredicateDescription);

      return this;
    }

    public Builder addStructurePredicateDescription(String value) {
      Preconditions.checkNotNull(value, "The value cannot be null!");

      this.structurePredicateDescription.add(value);

      return this;
    }

    /**
     * @param structurePredicateType the structurePredicateType to set
     */
    public Builder setStructurePredicateType(Set<String> structurePredicateType) {
      Preconditions.checkNotNull(structurePredicateType, "The structurePredicateType cannot be null!");

      this.structurePredicateType = new HashSet<>(structurePredicateType);

      return this;
    }

    public Builder addAllStructurePredicateType(Collection<String> structurePredicateType) {
      Preconditions.checkNotNull(structurePredicateType, "The structurePredicateType cannot be null!");

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
      Preconditions.checkNotNull(structureTypeClass, "The structureTypeClass cannot be null!");

      this.structureTypeClass = new HashSet<>(structureTypeClass);

      return this;
    }

    public Builder addAllStructureTypeClass(Collection<String> structureTypeClass) {
      Preconditions.checkNotNull(structureTypeClass, "The structureTypeClass cannot be null!");

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
      Preconditions.checkNotNull(structureTypeProperty, "The structureTypeProperty cannot be null!");

      this.structureTypeProperty = new HashSet<>(structureTypeProperty);

      return this;
    }

    public Builder addAllStructureTypeProperty(Collection<String> structureTypeProperty) {
      Preconditions.checkNotNull(structureTypeProperty, "The structureTypeProperty cannot be null!");

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
      Preconditions.checkNotNull(stoppedClasses, "The stoppedClasses cannot be null!");

      this.stoppedClasses = new HashSet<>(stoppedClasses);

      return this;
    }

    public Builder addStoppedClass(String stoppedClass) {
      Preconditions.checkNotNull(stoppedClass, "The stoppedClass cannot be null!");

      this.stoppedClasses.add(stoppedClass);

      return this;
    }

    public Builder setStoppedAttributes(List<String> stoppedAttributes) {
      Preconditions.checkNotNull(stoppedAttributes, "The stoppedAttributes cannot be null!");

      this.stoppedAttributes = new HashSet<>(stoppedAttributes);

      return this;
    }

    public Builder addStoppedAttributes(String stoppedAttribute) {
      Preconditions.checkNotNull(stoppedAttribute, "The stoppedAttribute cannot be null!");

      this.stoppedAttributes.add(stoppedAttribute);

      return this;
    }

    public void setGroupsAutoSelected(boolean groupsAutoSelected) {
      this.groupsAutoSelected = groupsAutoSelected;
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

  public static final String INSERT_TYPE_OBJECT_PROPERTY = "kb.insert.type.objectProperty";
  public static final String DEFAULT_INSERT_TYPE_OBJECT_PROPERTY = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Property";

  public static final String INSERT_TYPE_DATA_PROPERTY = "kb.insert.type.dataProperty";
  public static final String DEFAULT_INSERT_TYPE_DATA_PROPERTY = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Property";
  public static final String DEFAULT_INSERT_DATATYPE_PROPERTY_TYPE = "http://www.w3.org/2002/07/owl#DatatypeProperty";
  public static final String DEFAULT_INSERT_OBJECT_PROPERTY_TYPE = "http://www.w3.org/2002/07/owl#ObjectProperty";
  public static final String DEFAULT_INSERT_GRAPH = "http://odalic.eu";


  public static Builder builder() {
    return new Builder();
  }

  private final String name;

  private final boolean insertSupported;
//  private final String insertEndpoint;
  private final URI insertPrefixData;
  private final URI insertPrefixSchema;

  private final String endpoint;
  private final String login;
  private final String password;

  private final boolean groupsAutoSelected;
  private Set<String> structurePredicateLabel;
  private Set<String> structurePredicateDescription;
  private Set<String> structurePredicateType;
  private Set<String> structureTypeClass;
  private Set<String> structureTypeProperty;

  private final boolean fulltextEnabled;
  private final boolean useBifContains;
  private final String languageSuffix;

  private final SEARCH_CLASS_TYPE_MODE_VALUE classTypeMode;

  private final String structureInstanceOf;
  private final String structureDomain;
  private final String structureRange;

//  private final String insertGraph;

  private final String insertDefaultClass;
  private final String insertPredicateLabel;
  private final String insertPredicateAlternativeLabel;
  private final String insertPredicateSubclassOf;
  private final String insertPredicateSubPropertyOf;
  private final String insertTypeClass;
  private final String insertTypeObjectProperty;
  private final String insertTypeDataProperty;

  private final Set<String> stoppedClasses;
  private final Set<String> stoppedAttributes;

  private final boolean uriLabelHeuristicApplied;

  private PPProxyDefinition(final Builder builder) {
    Preconditions.checkArgument(!builder.structurePredicateLabel.isEmpty(), "No label predicate defined!");
    Preconditions.checkArgument(!builder.structurePredicateType.isEmpty(), "No type predicate defined!");

    this.name = builder.name;
    this.insertSupported = builder.insertSupported;
//    this.insertEndpoint = builder.insertEndpoint;
    this.insertPrefixData = builder.insertPrefixData;
    this.insertPrefixSchema = builder.insertPrefixSchema;

    this.endpoint = builder.endpoint;
    this.login = builder.login;
    this.password = builder.password;

    this.groupsAutoSelected = builder.groupsAutoSelected;
    setStructurePredicateLabel(builder.structurePredicateLabel);
    setStructurePredicateDescription(builder.structurePredicateDescription);
    setStructurePredicateType(builder.structurePredicateType);
    setStructureTypeClass(builder.structureTypeClass);
    setStructureTypeProperty(builder.structureTypeProperty);
    this.fulltextEnabled = builder.fulltextEnabled;
    this.useBifContains = builder.useBifContains;
    this.languageSuffix = builder.languageSuffix;
    this.classTypeMode = builder.classTypeMode;
    this.structureInstanceOf = builder.structureInstanceOf;
    this.structureDomain = builder.structureDomain;
    this.structureRange = builder.structureRange;
//    this.insertGraph = builder.insertGraph;
    this.insertDefaultClass = builder.insertDefaultClass;
    this.insertPredicateLabel = builder.insertPredicateLabel;
    this.insertPredicateAlternativeLabel = builder.insertPredicateAlternativeLabel;
    this.insertPredicateSubclassOf = builder.insertPredicateSubclassOf;
    this.insertPredicateSubPropertyOf = builder.insertPredicateSubPropertyOf;
    this.insertTypeClass = builder.insertTypeClass;
    this.insertTypeObjectProperty = builder.insertTypeObjectProperty;
    this.insertTypeDataProperty = builder.insertTypeDataProperty;
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
   * @return the login
   */
  public String getLogin() {
    return login;
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @return true is groups autodetection is enabled
   */
  public boolean isGroupsAutoSelected() {
    return groupsAutoSelected;
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

  public void setStructurePredicateLabel(Collection<? extends String> values) {
    structurePredicateLabel = ImmutableSet.copyOf(values);
  }

  public void setStructurePredicateDescription(Collection<? extends String> values) {
    structurePredicateDescription = ImmutableSet.copyOf(values);
  }

  public void setStructurePredicateType(Collection<? extends String> values) {
    structurePredicateType = ImmutableSet.copyOf(values);
  }

  public void setStructureTypeClass(Collection<? extends String> values) {
    structureTypeClass = ImmutableSet.copyOf(values);
  }

  public void setStructureTypeProperty(Collection<? extends String> values) {
    structureTypeProperty = ImmutableSet.copyOf(values);
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
  public PPProxyDefinition.SEARCH_CLASS_TYPE_MODE_VALUE getClassTypeMode() {
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

//  /**
//   * @return the insertGraph
//   */
//  public String getInsertGraph() {
//    return insertGraph;
//  }

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
   * @return the insertTypeObjectProperty
   */
  public String getInsertTypeObjectProperty() {
    return insertTypeObjectProperty;
  }

  /**
   * @return the insertTypeDataProperty
   */
  public String getInsertTypeDataProperty() {
    return insertTypeDataProperty;
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

  /**
   * @return insert endpoint
   */
//  public String getInsertEndpoint() {
//    return insertEndpoint;
//  }
}
