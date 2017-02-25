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

  public EntityValue(final Entity adaptee) {
    this.resource = adaptee.getResource();
    this.label = adaptee.getLabel();
    this.prefixed = adaptee.getPrefixed();
    this.prefix = adaptee.getPrefix();
    this.tail = adaptee.getTail();
  }

  /**
   * @return the label
   */
  @XmlElement
  @Nullable
  public String getLabel() {
    return this.label;
  }

  /**
   * @return the prefix
   */
  @XmlElement
  @Nullable
  public Prefix getPrefix() {
    return this.prefix;
  }

  /**
   * @return the prefixed form of the resource
   */
  @XmlElement
  @Nullable
  public String getPrefixed() {
    return this.prefixed;
  }

  /**
   * @return the resource ID
   */
  @XmlElement
  @Nullable
  public String getResource() {
    return this.resource;
  }

  /**
   * @return the tail
   */
  @XmlElement
  @Nullable
  public String getTail() {
    return this.tail;
  }

  /**
   * @param label the label to set
   */
  public void setLabel(final String label) {
    Preconditions.checkNotNull(label);

    this.label = label;
  }

  /**
   * @param prefix the prefix to set
   */
  public void setPrefix(final Prefix prefix) {
    this.prefix = prefix;
  }

  /**
   * @param prefixed the prefixed form of the resource to set
   */
  public void setPrefixed(final String prefixed) {
    this.prefixed = prefixed;
  }

  /**
   * @param resource the resource ID to set
   */
  public void setResource(final String resource) {
    Preconditions.checkNotNull(resource);

    this.resource = resource;
  }

  /**
   * @param tail the tail to set
   */
  public void setTail(final String tail) {
    this.tail = tail;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "EntityValue [resource=" + this.resource + ", label=" + this.label + ", prefixed="
        + this.prefixed + ", prefix=" + this.prefix + ", tail=" + this.tail + "]";
  }
}
