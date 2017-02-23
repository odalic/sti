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

  public EntityValue(Entity adaptee) {
    this.resource = adaptee.getResource();
    this.label = adaptee.getLabel();
    this.prefixed = adaptee.getPrefixed();
    this.prefix = adaptee.getPrefix() == null ? null : new PrefixValue(adaptee.getPrefix());
    this.tail = adaptee.getTail();
  }

  /**
   * @return the resource ID
   */
  @RdfProperty(value = "http://odalic.eu/internal/Entity/resource", datatype = "http://www.w3.org/2001/XMLSchema#string")
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
  @RdfProperty(value = "http://odalic.eu/internal/Entity/label", datatype = "http://www.w3.org/2001/XMLSchema#string")
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
  @RdfProperty(value = "http://odalic.eu/internal/Entity/prefixed", datatype = "http://www.w3.org/2001/XMLSchema#string")
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
  @RdfProperty("http://odalic.eu/internal/Entity/prefix")
  @Nullable
  public PrefixValue getPrefix() {
    return prefix;
  }

  /**
   * @param prefix the prefix to set
   */
  public void setPrefix(PrefixValue prefix) {
    this.prefix = prefix;
  }

  /**
   * @return the tail
   */
  @RdfProperty(value = "http://odalic.eu/internal/Entity/tail", datatype = "http://www.w3.org/2001/XMLSchema#string")
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
  
  public Entity toEntity() {
    return new Entity(prefix == null ? null : prefix.toPrefix(), prefix == null ? resource : tail, label);
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
