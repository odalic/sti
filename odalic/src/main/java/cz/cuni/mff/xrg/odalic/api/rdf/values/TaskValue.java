package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.openrdf.model.Resource;

import com.complexible.pinto.Identifiable;
import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.Task;

/**
 * Domain class {@link Task} adapted for RDF serialization.
 * 
 * @author Václav Brodec
 *
 */
@RdfsClass("http://odalic.eu/internal/Task")
public final class TaskValue implements Serializable, Identifiable {

  private static final long serialVersionUID = 1610346823333685091L;

  private String description;

  private ConfigurationValue configuration;

  private Resource identifiableResource;

  public TaskValue() {}

  public TaskValue(Task adaptee) {
    description = adaptee.getDescription();
    configuration = new ConfigurationValue(adaptee.getConfiguration());
  }

  /**
   * @return the description
   */
  @Nullable
  @RdfProperty(value = "http://odalic.eu/internal/Task/Description", datatype = "http://www.w3.org/2001/XMLSchema#string")
  public String getDescription() {
    return description;
  }

  /**
   * @param description the task description
   */
  public void setDescription(String description) {
    Preconditions.checkNotNull(description);

    this.description = description;
  }

  /**
   * @return the configuration
   */
  @RdfProperty("http://odalic.eu/internal/Task/Configuration")
  @Nullable
  public ConfigurationValue getConfiguration() {
    return configuration;
  }

  /**
   * @param configuration the configuration to set
   */
  public void setConfiguration(ConfigurationValue configuration) {
    Preconditions.checkNotNull(configuration);

    this.configuration = configuration;
  }

  /* (non-Javadoc)
   * @see com.complexible.pinto.Identifiable#id()
   */
  @Override
  public Resource id() {
    return this.identifiableResource;
  }

  /* (non-Javadoc)
   * @see com.complexible.pinto.Identifiable#id(org.openrdf.model.Resource)
   */
  @Override
  public void id(final Resource resource) {
    this.identifiableResource = resource;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "TaskValue [description=" + description + ", configuration=" + configuration + "]";
  }
}
