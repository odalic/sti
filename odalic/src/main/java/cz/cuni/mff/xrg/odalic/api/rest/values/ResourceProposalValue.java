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

import cz.cuni.mff.xrg.odalic.entities.ResourceProposal;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;

/**
 * Domain class {@link ResourceProposal} adapted for the REST API.
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlRootElement(name = "resourceProposal")
public final class ResourceProposalValue implements Serializable {

  private static final long serialVersionUID = 4650112693694357493L;

  private String label;

  private NavigableSet<String> alternativeLabels;

  private URI suffix;

  private NavigableSet<Entity> classes;

  public ResourceProposalValue() {
    this.alternativeLabels = ImmutableSortedSet.of();
    this.classes = ImmutableSortedSet.of();
  }

  /**
   * @return the alternative labels
   */
  @XmlElement
  public NavigableSet<String> getAlternativeLabels() {
    return this.alternativeLabels;
  }

  /**
   * @return the classes (in natural order)
   */
  @Nullable
  @XmlElement
  public NavigableSet<Entity> getClasses() {
    return this.classes;
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
   * @return the URI suffix
   */
  @Nullable
  @XmlElement
  public URI getSuffix() {
    return this.suffix;
  }

  /**
   * @param alternativeLabels the alternativeLabels to set
   */
  public void setAlternativeLabels(final Set<? extends String> alternativeLabels) {
    this.alternativeLabels = ImmutableSortedSet.copyOf(alternativeLabels);
  }

  /**
   * @param classes the classes to set
   */
  public void setClasses(final Set<? extends Entity> classes) {
    this.classes = ImmutableSortedSet.copyOf(classes);
  }

  /**
   * @param label the label to set
   */
  public void setLabel(final String label) {
    Preconditions.checkNotNull(label, "The label cannot be null!");

    this.label = label;
  }

  /**
   * @param suffix the suffix to set
   */
  public void setSuffix(final URI suffix) {
    this.suffix = suffix;
  }

  @Override
  public String toString() {
    return "ResourceProposalValue [label=" + this.label + ", alternativeLabels="
        + this.alternativeLabels + ", suffix=" + this.suffix + ", classes=" + this.classes + "]";
  }
}
