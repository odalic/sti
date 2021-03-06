package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.net.URI;
import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.entities.PropertyProposal;
import cz.cuni.mff.xrg.odalic.entities.PropertyType;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;

/**
 * Domain property {@link PropertyProposal} adapted for the REST API.
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlRootElement(name = "propertyProposal")
public final class PropertyProposalValue implements Serializable {

  private static final long serialVersionUID = 4650112693694357493L;

  private String label;

  private NavigableSet<String> alternativeLabels;

  private URI suffix;

  private Entity superProperty;

  private String domain;

  private String range;

  private PropertyType type;

  public PropertyProposalValue() {
    this.alternativeLabels = ImmutableSortedSet.of();
  }

  /**
   * @return the alternative labels
   */
  @XmlElement
  public NavigableSet<String> getAlternativeLabels() {
    return this.alternativeLabels;
  }

  /**
   * @return the domain
   */
  @Nullable
  @XmlElement
  public String getDomain() {
    return this.domain;
  }

  /**
   * @return the label
   */
  @Nullable
  @XmlElement
  public String getLabel() {
    return this.label;
  }

  /**
   * @return the range
   */
  @Nullable
  @XmlElement
  public String getRange() {
    return this.range;
  }

  /**
   * @return the URI suffix
   */
  @Nullable
  @XmlElement
  public URI getSuffix() {
    return this.suffix;
  }

  /**
   * @return the super property
   */
  @Nullable
  @XmlElement
  public Entity getSuperProperty() {
    return this.superProperty;
  }

  /**
   * @return the type
   */
  @Nullable
  @XmlElement
  public PropertyType getType() {
    return this.type;
  }

  /**
   * @param alternativeLabels the alternativeLabels to set
   */
  public void setAlternativeLabels(final Set<? extends String> alternativeLabels) {
    this.alternativeLabels = ImmutableSortedSet.copyOf(alternativeLabels);
  }

  /**
   * @param domain the domain to set
   */
  public void setDomain(final String domain) {
    this.domain = domain;
  }

  /**
   * @param label the label to set
   */
  public void setLabel(final String label) {
    Preconditions.checkNotNull(label, "The label cannot be null!");

    this.label = label;
  }

  /**
   * @param range the range to set
   */
  public void setRange(final String range) {
    this.range = range;
  }

  /**
   * @param suffix the suffix to set
   */
  public void setSuffix(final URI suffix) {
    this.suffix = suffix;
  }

  /**
   * @param superProperty the superProperty to set
   */
  public void setSuperProperty(final Entity superProperty) {
    this.superProperty = superProperty;
  }
  
  /**
   * @param type the type to set
   */
  public void setType(final PropertyType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "PropertyProposalValue [label=" + this.label + ", alternativeLabels="
        + this.alternativeLabels + ", suffix=" + this.suffix + ", superProperty="
        + this.superProperty + ", domain=" + this.domain + ", range=" + this.range + ", type="
        + this.type + "]";
  }
}
