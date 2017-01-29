package cz.cuni.mff.xrg.odalic.tasks;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.TaskAdapter;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.users.User;

/**
 * Task represents the single unit of work done by the Odalic core. Its configuration is
 * replaceable.
 * 
 * @author Václav Brodec
 *
 */
@XmlJavaTypeAdapter(TaskAdapter.class)
public final class Task implements Serializable {

  private static final long serialVersionUID = 1610346823333685091L;

  private final User owner;

  private final String id;

  private final String description;

  private final Date created;

  private Configuration configuration;

  private Input inputSnapshot;

  /**
   * Creates the task instance.
   * 
   * @param owner owner
   * @param id ID of the task
   * @param description the task description
   * @param created provided time of creation
   * @param configuration configuration of the task
   */
  public Task(final User owner, final String id, final String description, final Date created,
      final Configuration configuration) {
    Preconditions.checkNotNull(owner);
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(description);
    Preconditions.checkNotNull(created);
    Preconditions.checkNotNull(configuration);

    Preconditions.checkArgument(configuration.getInput().getOwner().equals(owner),
        "The task owner must also own the processed file!");

    this.owner = owner;
    this.id = id;
    this.description = description;
    this.created = created;
    this.configuration = configuration;
    this.inputSnapshot = null; // Invalidate.
  }

  /**
   * Creates the task instance and sets it creation date to now.
   * 
   * @param id ID of the task
   * @param description the task description
   * @param configuration configuration of the task
   */
  public Task(final User owner, final String id, final String description,
      final Configuration configuration) {
    this(owner, id, description, new Date(), configuration);
  }

  /**
   * @return the configuration
   */
  public Configuration getConfiguration() {
    return configuration;
  }

  /**
   * @param configuration the configuration to set
   */
  public void setConfiguration(Configuration configuration) {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkArgument(configuration.getInput().getOwner().equals(owner),
        "The task owner must also own the processed file!");

    this.configuration = configuration;
  }

  /**
   * Provides a snapshot of the input.
   * 
   * @return a snapshot of the input, {@code null} when there is none
   */
  @Nullable
  public Input getInputSnapshot() {
    return inputSnapshot;
  }

  /**
   * @param inputSnapshot input snapshot to set
   */
  public void setInputSnapshot(final Input inputSnapshot) {
    Preconditions.checkNotNull(inputSnapshot);

    this.inputSnapshot = inputSnapshot;
  }

  /**
   * @return the owner
   */
  public User getOwner() {
    return owner;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return the created
   */
  public Date getCreated() {
    return created;
  }

  /**
   * Computes hash code value for this object based solely on its ID.
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + owner.hashCode();
    result = prime * result + id.hashCode();
    return result;
  }

  /**
   * Compares for equality (comparable to other {@link Task} instances only, based solely on their
   * IDs).
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    Task other = (Task) obj;
    if (!owner.equals(other.owner)) {
      return false;
    }
    if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Task [owner=" + owner + ", id=" + id + ", description=" + description + ", created="
        + created + ", configuration=" + configuration + ", inputSnapshot=" + inputSnapshot + "]";
  }
}
