package cz.cuni.mff.xrg.odalic.files;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;
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
@XmlJavaTypeAdapter(FileValueOutputAdapter.class)
public final class File implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  private final User owner;

  private final String id;

  private final Date uploaded;

  private final URL location;

  private final boolean cached;

  private Format format;

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
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(uploaded);
    Preconditions.checkNotNull(owner);
    Preconditions.checkNotNull(location);
    Preconditions.checkNotNull(format);

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
   * @return the uploaded
   */
  public Date getUploaded() {
    return uploaded;
  }

  /**
   * @return the location
   */
  public URL getLocation() {
    return location;
  }

  /**
   * @return cached
   */
  public boolean isCached() {
    return cached;
  }

  /**
   * @return the format
   */
  public Format getFormat() {
    return format;
  }

  /**
   * @param format the format to set
   */
  public void setFormat(Format format) {
    Preconditions.checkNotNull(format);

    this.format = format;
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
    result = prime * result + owner.hashCode();
    result = prime * result + id.hashCode();
    return result;
  }

  /**
   * Compares for equivalence (only other File description with the same components passes).
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
    File other = (File) obj;
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
    return "File [owner=" + owner + ", id=" + id + ", uploaded=" + uploaded + ", location="
        + location + ", format=" + format + ", cached=" + cached + "]";
  }
}
