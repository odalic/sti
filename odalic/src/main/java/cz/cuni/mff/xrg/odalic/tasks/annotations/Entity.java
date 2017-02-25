package cz.cuni.mff.xrg.odalic.tasks.annotations;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.EntityAdapter;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.Prefix;


/**
 * Groups the resource ID and its label in one handy class.
 *
 * @author Václav Brodec
 */
@XmlJavaTypeAdapter(EntityAdapter.class)
public final class Entity implements Comparable<Entity>, Serializable {

  public static final String PREFIX_SEPARATOR = ":";

  private static final long serialVersionUID = -3001706805535088480L;

  /**
   * Creates new entity representation.
   *
   * @param prefix resource ID prefix
   * @param resource entity resource ID
   * @param label label
   */
  public static Entity of(final @Nullable Prefix prefix, final String resourceId,
      final String label) {
    Preconditions.checkNotNull(resourceId);
    Preconditions.checkNotNull(label);

    if (prefix == null) {
      return of(resourceId, label);
    }

    final String prefixedPart = prefix.getWhat();
    Preconditions.checkArgument(resourceId.startsWith(prefixedPart));

    final String suffix = resourceId.substring(prefixedPart.length());

    return new Entity(prefix, suffix, label);
  }

  /**
   * Creates new entity representation (without prefix).
   *
   * @param resource entity resource ID
   * @param label label
   */
  public static Entity of(final String resourceId, final String label) {
    Preconditions.checkNotNull(resourceId);
    Preconditions.checkNotNull(label);

    return new Entity(null, resourceId, label);
  }

  private final Prefix prefix;

  private final String tail;

  private final String label;

  public Entity(final Prefix prefix, final String suffix, final String label) {
    Preconditions.checkNotNull(suffix);
    Preconditions.checkNotNull(label);

    this.prefix = prefix;
    this.tail = suffix;
    this.label = label;
  }

  /**
   * Compares the entities by their resource ID lexicographically.
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   * @see java.lang.String#compareTo(String) for the definition of resource ID comparison
   */
  @Override
  public int compareTo(final Entity o) {
    return getResource().compareTo(o.getResource());
  }

  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    }
    if (object == null) {
      return false;
    }
    if (getClass() != object.getClass()) {
      return false;
    }
    final Entity other = (Entity) object;
    if (!getResource().equals(other.getResource())) {
      return false;
    }
    return true;
  }

  /**
   * @return the label
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * @return the resource ID prefix
   */
  @Nullable
  public Prefix getPrefix() {
    return this.prefix;
  }

  /**
   * @return prefix{@value #PREFIX_SEPARATOR}tail, if prefix not {@code null}, otherwise the same as
   *         {@link #getResource()}
   */
  public String getPrefixed() {
    if (this.prefix == null) {
      return this.tail;
    }

    return this.prefix.getWith() + PREFIX_SEPARATOR + this.tail;
  }

  /**
   * @return the expanded resource ID
   */
  public String getResource() {
    if (this.prefix == null) {
      return this.tail;
    }

    return this.prefix.getWhat() + this.tail;
  }

  /**
   * @return the part of the resources ID that follows the part substitued by prefix, {@code null}
   *         if the prefix is not defined
   */
  @Nullable
  public String getTail() {
    if (this.prefix == null) {
      return null;
    }

    return this.tail;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + getResource().hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "Entity [prefix=" + this.prefix + ", suffix=" + this.tail + ", label=" + this.label
        + ", getResource()=" + getResource() + "]";
  }
}
