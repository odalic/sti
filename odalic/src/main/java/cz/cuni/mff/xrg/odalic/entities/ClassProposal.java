package cz.cuni.mff.xrg.odalic.entities;

import java.io.Serializable;
import java.net.URI;
import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.ClassProposalAdapter;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;

/**
 * <p>
 * Model of the proposal for a new class in the primary base, that the user provides to the server.
 * </p>
 *
 * <p>
 * Every class used in the user feedback must already exist in any of the present bases, if it does
 * not, the user is encouraged to enter it first as a proposal.
 * </p>
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(ClassProposalAdapter.class)
public final class ClassProposal implements Serializable {

  private static final long serialVersionUID = 1780898655030310168L;

  private final String label;

  private final NavigableSet<String> alternativeLabels;

  private final URI suffix;

  private final Entity superClass;

  public ClassProposal(final String label, final Set<? extends String> alternativeLabels,
      final URI suffix, final Entity superClass) {
    Preconditions.checkNotNull(label);
    Preconditions.checkArgument((suffix == null) || !suffix.isAbsolute(),
        "The suffix must be a relative URI!");

    this.label = label;
    this.alternativeLabels = ImmutableSortedSet.copyOf(alternativeLabels);
    this.suffix = suffix;
    this.superClass = superClass;
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
    final ClassProposal other = (ClassProposal) obj;
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

  /**
   * @return the super class
   */
  @Nullable
  public Entity getSuperClass() {
    return this.superClass;
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
    return "ClassProposal [label=" + this.label + ", alternativeLabels=" + this.alternativeLabels
        + ", suffix=" + this.suffix + ", superClass=" + this.superClass + "]";
  }
}
