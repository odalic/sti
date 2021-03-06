package cz.cuni.mff.xrg.odalic.files;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.regex.Pattern;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.FileValueOutputAdapter;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.users.User;

/**
 * File description.
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(FileValueOutputAdapter.class)
public final class File implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  public static final Pattern VALID_ID_PATTERN = Pattern.compile("[-a-zA-Z0-9_., ]+");

  private final User owner;

  private final String id;

  private final Date uploaded;

  private final URL location;

  private final boolean cached;

  private final Format format;

  /**
   * Create new file description.
   *
   * @param owner owner
   * @param id file ID
   * @param uploaded time of upload
   * @param location file location
   * @param format CSV file format
   * @param cached boolean
   */
  public File(final User owner, final String id, final Date uploaded, final URL location,
      final Format format, final boolean cached) {
    Preconditions.checkNotNull(id, "The id cannot be null!");
    Preconditions.checkNotNull(uploaded, "The uploaded cannot be null!");
    Preconditions.checkNotNull(owner, "The owner cannot be null!");
    Preconditions.checkNotNull(location, "The location cannot be null!");
    Preconditions.checkNotNull(format, "The format cannot be null!");

    Preconditions.checkArgument(!id.isEmpty(), "The file identifier is empty!");
    Preconditions.checkArgument(VALID_ID_PATTERN.matcher(id).matches(),
        "The file identifier contains illegal characters!");

    this.owner = owner;
    this.id = id;
    this.uploaded = uploaded;
    this.location = location;
    this.format = format;
    this.cached = cached;
  }

  /**
   * Create new file description for a file uploaded now.
   *
   * @param owner owner
   * @param id file ID
   * @param location file location
   * @param format CSV file format
   * @param cached cached
   */
  public File(final User owner, final String id, final URL location, final Format format,
      final boolean cached) {
    this(owner, id, new Date(), location, format, cached);
  }


  /**
   * Compares for equivalence (only other File description with the same components passes).
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
    final File other = (File) obj;
    if (!this.owner.equals(other.owner)) {
      return false;
    }
    if (!this.id.equals(other.id)) {
      return false;
    }
    return true;
  }

  /**
   * @return the format
   */
  public Format getFormat() {
    return this.format;
  }

  /**
   * @return the id
   */
  public String getId() {
    return this.id;
  }

  /**
   * @return the location
   */
  public URL getLocation() {
    return this.location;
  }

  /**
   * @return the owner
   */
  public User getOwner() {
    return this.owner;
  }

  /**
   * @return the uploaded
   */
  public Date getUploaded() {
    return this.uploaded;
  }

  /**
   * Computes the hash code based on all components.
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

  /**
   * @return cached
   */
  public boolean isCached() {
    return this.cached;
  }

  @Override
  public String toString() {
    return "File [owner=" + this.owner + ", id=" + this.id + ", uploaded=" + this.uploaded
        + ", location=" + this.location + ", format=" + this.format + ", cached=" + this.cached
        + "]";
  }
}
