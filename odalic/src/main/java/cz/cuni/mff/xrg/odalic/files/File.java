package cz.cuni.mff.xrg.odalic.files;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.FileValueOutputAdapter;
import cz.cuni.mff.xrg.odalic.input.CsvConfiguration;

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

  private final String id;

  private final Date uploaded;

  private final String owner;
  
  private final URL location;
  
  private final CsvConfiguration parsingConfiguration;
  
  private final boolean cached;

  /**
   * Create new file description.
   * 
   * @param id file ID
   * @param uploaded time of upload
   * @param owner file owner description
   * @param location file location
   * @param parsingConfiguration parser configuration
   * @param cached boolean
   */
  public File(final String id, final Date uploaded, final String owner, final URL location, final CsvConfiguration parsingConfiguration, final boolean cached) {
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(uploaded);
    Preconditions.checkNotNull(owner);
    Preconditions.checkNotNull(location);
    Preconditions.checkNotNull(parsingConfiguration);
    
    this.id = id;
    this.uploaded = uploaded;
    this.owner = owner;
    this.location = location;
    this.parsingConfiguration = parsingConfiguration;
    this.cached = cached;
  }
  
  /**
   * Create new file description for a file uploaded now.
   * 
   * @param id file ID
   * @param owner file owner description
   * @param location file location
   * @param cached cached
   */
  public File(final String id, final String owner, final URL location, final CsvConfiguration parsingConfiguration, final boolean cached) {
    this(id, new Date(), owner, location, parsingConfiguration, cached);
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
   * @return the owner
   */
  public String getOwner() {
    return owner;
  }

  /**
   * @return the location
   */
  public URL getLocation() {
    return location;
  }
  
  /**
   * @return the parser configuration
   */
  public CsvConfiguration getParsingConfiguration() {
    return parsingConfiguration;
  }

  /**
   * @return cached
   */
  public boolean isCached() {
    return cached;
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
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "File [id=" + id + ", uploaded=" + uploaded + ", owner=" + owner + ", location="
        + location + ", cached=" + cached + "]";
  }
}
