package cz.cuni.mff.xrg.odalic.entities;

import java.io.Serializable;
import java.net.URI;
import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.ResourceProposalAdapter;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;

/**
 * <p>
 * Model of the proposal for a new entity (representing a disambiguated cell) in the primary base,
 * that the user provides to the server.
 * </p>
 *
 * <p>
 * Every entity used in the user feedback must already exist in any of the present bases, if it does
 * not, the user is encouraged to enter it first as a proposal.
 * </p>
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(ResourceProposalAdapter.class)
public final class ResourceProposal implements Serializable {

  private static final long serialVersionUID = -5242665067080019900L;

  private final String label;

  private final NavigableSet<String> alternativeLabels;

  private final URI suffix;

  private final Set<Entity> classes;

  public ResourceProposal(final String label, final Set<? extends String> alternativeLabels,
      final URI suffix, final Set<? extends Entity> classes) {
    Preconditions.checkNotNull(label, "The label cannot be null!");
    Preconditions.checkArgument((suffix == null) || !suffix.isAbsolute(),
        "The suffix must be a relative URI!");

    this.label = label;
    this.alternativeLabels = ImmutableSortedSet.copyOf(alternativeLabels);
    this.suffix = suffix;
    this.classes = ImmutableSet.copyOf(classes);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ResourceProposal other = (ResourceProposal) obj;
    if (this.suffix == null) {
      if (other.suffix != null) {
        return false;
      }
    } else if (!this.suffix.equals(other.suffix)) {
      return false;
    }
    return true;
  }

  /**
   * @return the alternative labels
   */
  public NavigableSet<String> getAlternativeLabels() {
    return this.alternativeLabels;
  }

  /**
   * @return the classes the entity is a member of
   */
  @Nullable
  public Set<Entity> getClasses() {
    return this.classes;
  }

  /**
   * @return the label
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * @return the URI suffix
   */
  public URI getSuffix() {
    return this.suffix;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.suffix == null) ? 0 : this.suffix.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "ResourceProposal [label=" + this.label + ", alternativeLabels=" + this.alternativeLabels
        + ", suffix=" + this.suffix + ", classes=" + this.classes + "]";
  }
}
