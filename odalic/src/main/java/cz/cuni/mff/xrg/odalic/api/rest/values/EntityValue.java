package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.Prefix;

/**
 * Domain class {@link Entity} adapted for REST API.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "entity")
public final class EntityValue implements Serializable {

  private static final long serialVersionUID = 5750987769573292984L;

  private String resource;

  private String label;

  private String prefixed;
  
  private Prefix prefix;
  
  private String tail;

  public EntityValue() {}

  public EntityValue(Entity adaptee) {
    this.resource = adaptee.getResource();
    this.label = adaptee.getLabel();
    this.prefixed = adaptee.getPrefixed();
    this.prefix = adaptee.getPrefix();
    this.tail = adaptee.getTail();
  }

  /**
   * @return the resource ID
   */
  @XmlElement
  @Nullable
  public String getResource() {
    return resource;
  }

  /**
   * @param resource the resource ID to set
   */
  public void setResource(String resource) {
    Preconditions.checkNotNull(resource);

    this.resource = resource;
  }

  /**
   * @return the label
   */
  @XmlElement
  @Nullable
  public String getLabel() {
    return label;
  }

  /**
   * @param label the label to set
   */
  public void setLabel(String label) {
    Preconditions.checkNotNull(label);

    this.label = label;
  }

  /**
   * @return the prefixed form of the resource
   */
  @XmlElement
  @Nullable
  public String getPrefixed() {
    return prefixed;
  }

  /**
   * @param prefixed the prefixed form of the resource to set
   */
  public void setPrefixed(String prefixed) {
    this.prefixed = prefixed;
  }

  /**
   * @return the prefix
   */
  @XmlElement
  @Nullable
  public Prefix getPrefix() {
    return prefix;
  }

  /**
   * @param prefix the prefix to set
   */
  public void setPrefix(Prefix prefix) {
    this.prefix = prefix;
  }

  /**
   * @return the tail
   */
  @XmlElement
  @Nullable
  public String getTail() {
    return tail;
  }

  /**
   * @param tail the tail to set
   */
  public void setTail(String tail) {
    this.tail = tail;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "EntityValue [resource=" + resource + ", label=" + label + ", prefixed=" + prefixed
        + ", prefix=" + prefix + ", tail=" + tail + "]";
  }
}
