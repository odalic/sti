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

  public TaskValue(final Task adaptee) {
    this.description = adaptee.getDescription();
    this.configuration = new ConfigurationValue(adaptee.getConfiguration());
  }

  /**
   * @return the configuration
   */
  @RdfProperty("http://odalic.eu/internal/Task/configuration")
  @Nullable
  public ConfigurationValue getConfiguration() {
    return this.configuration;
  }

  /**
   * @return the description
   */
  @Nullable
  @RdfProperty(value = "http://odalic.eu/internal/Task/description",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  public String getDescription() {
    return this.description;
  }

  @Override
  public Resource id() {
    return this.identifiableResource;
  }

  @Override
  public void id(final Resource resource) {
    this.identifiableResource = resource;
  }

  /**
   * @param configuration the configuration to set
   */
  public void setConfiguration(final ConfigurationValue configuration) {
    Preconditions.checkNotNull(configuration);

    this.configuration = configuration;
  }

  /**
   * @param description the task description
   */
  public void setDescription(final String description) {
    Preconditions.checkNotNull(description);

    this.description = description;
  }

  @Override
  public String toString() {
    return "TaskValue [description=" + this.description + ", configuration=" + this.configuration
        + "]";
  }
}
