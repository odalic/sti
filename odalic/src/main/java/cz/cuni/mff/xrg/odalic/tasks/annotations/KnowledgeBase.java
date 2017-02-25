package cz.cuni.mff.xrg.odalic.tasks.annotations;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.KnowledgeBaseAdapter;

/**
 * Knowledge base identifier.
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(KnowledgeBaseAdapter.class)
public final class KnowledgeBase implements Serializable, Comparable<KnowledgeBase> {

  private static final long serialVersionUID = 2241360833757117714L;

  private final String name;

  public KnowledgeBase(final String name) {
    Preconditions.checkNotNull(name);

    this.name = name;
  }

  /**
   * Compares the names.
   *
   * @param other other base
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(final KnowledgeBase other) {
    return this.name.compareTo(other.name);
  }

  /**
   * Compares for equality (only other knowledge base instance with the same name passes, for now).
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    final KnowledgeBase other = (KnowledgeBase) obj;
    if (this.name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!this.name.equals(other.name)) {
      return false;
    }
    return true;
  }

  /**
   * @return the name
   */
  public String getName() {
    return this.name;
  }


  /**
   * Computes hash code (for now) based on the name.
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.name == null) ? 0 : this.name.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "KnowledgeBase [name=" + this.name + "]";
  }
}
