package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomJsonDateDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomJsonDateSerializer;
import cz.cuni.mff.xrg.odalic.tasks.Task;

@XmlRootElement(name = "task")
public final class TaskValue implements Serializable {

  private static final long serialVersionUID = 1610346823333685091L;

  @XmlElement
  private String id;
  
  @JsonSerialize(using = CustomJsonDateSerializer.class)
  @JsonDeserialize(using = CustomJsonDateDeserializer.class)
  @XmlElement
  private Date created;

  @XmlElement
  private ConfigurationValue configuration;
  
  public TaskValue() {
    id = null;
    created = null;
    configuration = null;
  }

  /**
   * @param id
   * @param created
   */
  public TaskValue(Task adaptee) {
    id = adaptee.getId();
    created = adaptee.getCreated();
    configuration = new ConfigurationValue(adaptee.getConfiguration());
  }

  /**
   * @return the id
   */
  @Nullable
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    Preconditions.checkNotNull(id);
    
    this.id = id;
  }

  /**
   * @return the created
   */
  @Nullable
  public Date getCreated() {
    return created;
  }

  /**
   * @param created the created to set
   */
  public void setCreated(Date created) {
    Preconditions.checkNotNull(created);
    
    this.created = created;
  }

  /**
   * @return the configuration
   */
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
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "TaskValue [id=" + id + ", created=" + created + ", configuration=" + configuration
        + "]";
  }
}