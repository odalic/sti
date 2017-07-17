package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Preconditions;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;

/**
 * Domain class {@link KnowledgeBase} adapted for REST API input.
 * 
 * Introduced to maintain backward compatibility with the previous version of REST API which kept
 * the name encapsulated within an object.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "knowledgeBase")
@JsonIgnoreProperties(ignoreUnknown = true)
public final class KnowledgeBaseNameValue
    implements Serializable, Comparable<KnowledgeBaseNameValue> {

  private static final long serialVersionUID = -1264923889540290812L;

  private String name;

  public KnowledgeBaseNameValue() {}

  public KnowledgeBaseNameValue(final String name) {
    Preconditions.checkNotNull(name, "The name cannot be null!");
    
    this.name = name;
  }
  
  public KnowledgeBaseNameValue(final KnowledgeBase adaptee) {
    this.name = adaptee.getName();
  }

  /**
   * @return the name
   */
  @XmlElement
  @Nullable
  public String getName() {
    return this.name;
  }

  /**
   * @param name the name to set
   */
  public void setName(final String name) {
    Preconditions.checkNotNull(name, "The name cannot be null!");

    this.name = name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

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
    KnowledgeBaseNameValue other = (KnowledgeBaseNameValue) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(final KnowledgeBaseNameValue other) {
    if (this.name == null) {
      if (other.name == null) {
        return 0;
      }

      return -1;
    } else if (other.name == null) {
      return 1;
    }

    return this.name.compareTo(other.name);
  }

  @Override
  public String toString() {
    return "KnowledgeBaseValueInput [name=" + name + "]";
  }
}
