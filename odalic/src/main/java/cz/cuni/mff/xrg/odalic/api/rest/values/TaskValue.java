package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomDateJsonDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomDateJsonSerializer;
import cz.cuni.mff.xrg.odalic.tasks.Task;

/**
 * Domain class {@link Task} adapted for REST API.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "task")
public final class TaskValue implements Serializable {

  private static final long serialVersionUID = 1610346823333685091L;

  private String id;

  private String description;

  private Date created;

  private ConfigurationValue configuration;

  public TaskValue() {}

  public TaskValue(final Task adaptee) {
    this.id = adaptee.getId();
    this.description = adaptee.getDescription();
    this.created = adaptee.getCreated();
    this.configuration = new ConfigurationValue(adaptee.getConfiguration());
  }

  /**
   * @return the configuration
   */
  @XmlElement
  @Nullable
  public ConfigurationValue getConfiguration() {
    return this.configuration;
  }

  /**
   * @return the created
   */
  @XmlElement
  @JsonSerialize(using = CustomDateJsonSerializer.class)
  @JsonDeserialize(using = CustomDateJsonDeserializer.class)
  @Nullable
  public Date getCreated() {
    return this.created;
  }

  /**
   * @return the description
   */
  @XmlElement
  @Nullable
  public String getDescription() {
    return this.description;
  }

  /**
   * @return the id
   */
  @XmlElement
  @Nullable
  public String getId() {
    return this.id;
  }

  /**
   * @param configuration the configuration to set
   */
  public void setConfiguration(final ConfigurationValue configuration) {
    Preconditions.checkNotNull(configuration, "The configuration cannot be null!");

    this.configuration = configuration;
  }

  /**
   * @param created the created to set
   */
  public void setCreated(final Date created) {
    Preconditions.checkNotNull(created, "The created cannot be null!");

    this.created = created;
  }

  /**
   * @param description the task description
   */
  public void setDescription(final String description) {
    Preconditions.checkNotNull(description, "The description cannot be null!");

    this.description = description;
  }

  /**
   * @param id the id to set
   */
  public void setId(final String id) {
    Preconditions.checkNotNull(id, "The id cannot be null!");

    this.id = id;
  }

  @Override
  public String toString() {
    return "TaskValue [id=" + this.id + ", description=" + this.description + ", created="
        + this.created + ", configuration=" + this.configuration + "]";
  }
}
