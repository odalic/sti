package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;

/**
 * Domain class {@link Entity} adapted for RDF serialization.
 *
 * @author Václav Brodec
 *
 */
@RdfsClass("http://odalic.eu/internal/Entity")
public final class EntityValue implements Serializable {

  private static final long serialVersionUID = 5750987769573292984L;

  private String resource;

  private String label;

  private String prefixed;

  private PrefixValue prefix;

  private String tail;

  public EntityValue() {}

  public EntityValue(final Entity adaptee) {
    this.resource = adaptee.getResource();
    this.label = adaptee.getLabel();
    this.prefixed = adaptee.getPrefixed();
    this.prefix = adaptee.getPrefix() == null ? null : new PrefixValue(adaptee.getPrefix());
    this.tail = adaptee.getTail();
  }

  /**
   * @return the label
   */
  @RdfProperty(value = "http://odalic.eu/internal/Entity/label",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  @Nullable
  public String getLabel() {
    return this.label;
  }

  /**
   * @return the prefix
   */
  @RdfProperty("http://odalic.eu/internal/Entity/prefix")
  @Nullable
  public PrefixValue getPrefix() {
    return this.prefix;
  }

  /**
   * @return the prefixed form of the resource
   */
  @RdfProperty(value = "http://odalic.eu/internal/Entity/prefixed",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  @Nullable
  public String getPrefixed() {
    return this.prefixed;
  }

  /**
   * @return the resource ID
   */
  @RdfProperty(value = "http://odalic.eu/internal/Entity/resource",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  @Nullable
  public String getResource() {
    return this.resource;
  }

  /**
   * @return the tail
   */
  @RdfProperty(value = "http://odalic.eu/internal/Entity/tail",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
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
  public void setPrefix(final PrefixValue prefix) {
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

  public Entity toEntity() {
    return new Entity(this.prefix == null ? null : this.prefix.toPrefix(),
        this.prefix == null ? this.resource : this.tail, this.label);
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
