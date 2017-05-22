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

import cz.cuni.mff.xrg.odalic.api.rest.adapters.PropertyProposalAdapter;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;

/**
 * <p>
 * Model of the proposal for a new property in the primary base, that the user provides to the
 * server.
 * </p>
 *
 * <p>
 * Every property used in the user feedback must already exist in any of the present bases, if it
 * does not, the user is encouraged to enter it first as a proposal.
 * </p>
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(PropertyProposalAdapter.class)
public final class PropertyProposal implements Serializable {

  private static final long serialVersionUID = -5534904798672086568L;

  private final String label;

  private final NavigableSet<String> alternativeLabels;

  private final URI suffix;

  private final Entity superProperty;

  private final String domain;

  private final String range;
  
  private final PropertyType type;

  public PropertyProposal(final String label, final Set<? extends String> alternativeLabels,
      final URI suffix, final Entity superProperty, final String domain, final String range,
      @Nullable final PropertyType type) {
    Preconditions.checkNotNull(label);
    Preconditions.checkArgument((suffix == null) || !suffix.isAbsolute(),
        "The suffix must be a relative URI!");

    this.label = label;
    this.alternativeLabels = ImmutableSortedSet.copyOf(alternativeLabels);
    this.suffix = suffix;
    this.superProperty = superProperty;
    this.domain = domain;
    this.range = range;
    this.type = type == null ? PropertyType.OBJECT : type;
  }

  /**
   * @return the alternative labels
   */
  public NavigableSet<String> getAlternativeLabels() {
    return this.alternativeLabels;
  }

  /**
   * @return the domain
   */
  public String getDomain() {
    return this.domain;
  }

  /**
   * @return the label
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * @return the range
   */
  public String getRange() {
    return this.range;
  }

  /**
   * @return the URI suffix
   */
  public URI getSuffix() {
    return this.suffix;
  }

  /**
   * @return the super property
   */
  @Nullable
  public Entity getSuperProperty() {
    return this.superProperty;
  }

  /**
   * @return the type
   */
  public PropertyType getType() {
    return this.type;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + alternativeLabels.hashCode();
    result = prime * result + ((domain == null) ? 0 : domain.hashCode());
    result = prime * result + label.hashCode();
    result = prime * result + ((range == null) ? 0 : range.hashCode());
    result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
    result = prime * result + ((superProperty == null) ? 0 : superProperty.hashCode());
    result = prime * result + type.hashCode();
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PropertyProposal other = (PropertyProposal) obj;
    if (!alternativeLabels.equals(other.alternativeLabels)) {
      return false;
    }
    if (domain == null) {
      if (other.domain != null) {
        return false;
      }
    } else if (!domain.equals(other.domain)) {
      return false;
    }
    if (!label.equals(other.label)) {
      return false;
    }
    if (range == null) {
      if (other.range != null) {
        return false;
      }
    } else if (!range.equals(other.range)) {
      return false;
    }
    if (suffix == null) {
      if (other.suffix != null) {
        return false;
      }
    } else if (!suffix.equals(other.suffix)) {
      return false;
    }
    if (superProperty == null) {
      if (other.superProperty != null) {
        return false;
      }
    } else if (!superProperty.equals(other.superProperty)) {
      return false;
    }
    if (!type.equals(other.type)) {
      return false;
    }
    return true;
  }
}
