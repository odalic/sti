package cz.cuni.mff.xrg.odalic.tasks;

import java.io.Serializable;
import java.util.Date;
import java.util.regex.Pattern;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.TaskAdapter;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.users.User;

/**
 * Task represents the single unit of work done by the Odalic core. Its configuration is
 * replaceable.
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(TaskAdapter.class)
public final class Task implements Serializable {

  private static final long serialVersionUID = 1610346823333685091L;

  public static final Pattern VALID_ID_PATTERN = Pattern.compile("[-a-zA-Z0-9_., ]+");

  private final User owner;

  private final String id;

  private final String description;

  private final Date created;

  private final Configuration configuration;

  /**
   * Creates the task instance and sets it creation date to now.
   *
   * @param owner owner
   * @param id ID of the task
   * @param description the task description
   * @param configuration configuration of the task
   */
  public Task(final User owner, final String id, final String description,
      final Configuration configuration) {
    this(owner, id, description, new Date(), configuration);
  }

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

    Preconditions.checkArgument(!id.isEmpty(), "The task identifier is empty!");
    Preconditions.checkArgument(VALID_ID_PATTERN.matcher(id).matches(),
        "The task identifier contains illegal characters!");
    Preconditions.checkArgument(configuration.getInput().getOwner().equals(owner),
        "The task owner must also own the processed file!");

    this.owner = owner;
    this.id = id;
    this.description = description;
    this.created = created;
    this.configuration = configuration;
  }

  /**
   * Compares for equality (comparable to other {@link Task} instances only, based solely on their
   * IDs).
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Task other = (Task) obj;
    if (!this.owner.equals(other.owner)) {
      return false;
    }
    if (!this.id.equals(other.id)) {
      return false;
    }
    return true;
  }

  /**
   * @return the configuration
   */
  public Configuration getConfiguration() {
    return this.configuration;
  }

  /**
   * @return the created
   */
  public Date getCreated() {
    return this.created;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * @return the id
   */
  public String getId() {
    return this.id;
  }

  /**
   * @return the owner
   */
  public User getOwner() {
    return this.owner;
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
    result = (prime * result) + this.owner.hashCode();
    result = (prime * result) + this.id.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "Task [owner=" + this.owner + ", id=" + this.id + ", description=" + this.description
        + ", created=" + this.created + ", configuration=" + this.configuration + "]";
  }
}
