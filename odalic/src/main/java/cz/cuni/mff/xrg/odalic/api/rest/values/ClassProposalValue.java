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

import cz.cuni.mff.xrg.odalic.entities.ClassProposal;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;

/**
 * Domain class {@link ClassProposal} adapted for the REST API.
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlRootElement(name = "classProposal")
public final class ClassProposalValue implements Serializable {

  private static final long serialVersionUID = 4650112693694357493L;

  private String label;

  private NavigableSet<String> alternativeLabels;

  private URI suffix;

  private Entity superClass;

  public ClassProposalValue() {
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
   * @return the super class
   */
  @Nullable
  @XmlElement
  public Entity getSuperClass() {
    return this.superClass;
  }

  /**
   * @param alternativeLabels the alternativeLabels to set
   */
  public void setAlternativeLabels(final Set<? extends String> alternativeLabels) {
    this.alternativeLabels = ImmutableSortedSet.copyOf(alternativeLabels);
  }

  /**
   * @param label the label to set
   */
  public void setLabel(final String label) {
    Preconditions.checkNotNull(label);

    this.label = label;
  }

  /**
   * @param suffix the suffix to set
   */
  public void setSuffix(final URI suffix) {
    this.suffix = suffix;
  }

  /**
   * @param superClass the superClass to set
   */
  public void setSuperClass(final Entity superClass) {
    this.superClass = superClass;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ClassProposalValue [label=" + this.label + ", alternativeLabels="
        + this.alternativeLabels + ", suffix=" + this.suffix + ", superClass=" + this.superClass
        + "]";
  }
}
