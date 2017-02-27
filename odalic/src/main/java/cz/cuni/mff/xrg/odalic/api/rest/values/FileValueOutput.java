package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomDateJsonDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomDateJsonSerializer;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.users.User;

/**
 * Domain class {@link File} adapted for REST API output.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "file")
public final class FileValueOutput implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  private User owner;

  private String id;

  private Date uploaded;

  private URL location;

  private Format format;

  private boolean cached;

  public FileValueOutput() {}

  public FileValueOutput(final File adaptee) {
    this.owner = adaptee.getOwner();
    this.id = adaptee.getId();
    this.uploaded = adaptee.getUploaded();
    this.location = adaptee.getLocation();
    this.format = adaptee.getFormat();
    this.cached = adaptee.isCached();
  }

  /**
   * @return the format
   */
  @XmlElement
  @Nullable
  public Format getFormat() {
    return this.format;
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
   * @return the location
   */
  @XmlElement
  @Nullable
  public URL getLocation() {
    return this.location;
  }

  /**
   * @return the owner
   */
  @XmlElement
  @Nullable
  public User getOwner() {
    return this.owner;
  }

  /**
   * @return the uploaded
   */
  @JsonSerialize(using = CustomDateJsonSerializer.class)
  @JsonDeserialize(using = CustomDateJsonDeserializer.class)
  @XmlElement
  @Nullable
  public Date getUploaded() {
    return this.uploaded;
  }

  /**
   * @return cached
   */
  @XmlElement
  public boolean isCached() {
    return this.cached;
  }

  /**
   * @param cached cached
   */
  public void setCached(final boolean cached) {
    this.cached = cached;
  }

  /**
   * @param format the format to set
   */
  public void setFormat(final Format format) {
    Preconditions.checkNotNull(format);

    this.format = format;
  }

  /**
   * @param id the id to set
   */
  public void setId(final String id) {
    Preconditions.checkNotNull(id);

    this.id = id;
  }

  /**
   * @param location the location to set
   */
  public void setLocation(final URL location) {
    Preconditions.checkNotNull(location);

    this.location = location;
  }

  /**
   * @param owner the owner to set
   */
  public void setOwner(final User owner) {
    Preconditions.checkNotNull(owner);

    this.owner = owner;
  }

  /**
   * @param uploaded the uploaded to set
   */
  public void setUploaded(final Date uploaded) {
    Preconditions.checkNotNull(uploaded);

    this.uploaded = uploaded;
  }

  @Override
  public String toString() {
    return "FileValueOutput [owner=" + this.owner + ", id=" + this.id + ", uploaded="
        + this.uploaded + ", location=" + this.location + ", format=" + this.format + ", cached="
        + this.cached + "]";
  }
}
