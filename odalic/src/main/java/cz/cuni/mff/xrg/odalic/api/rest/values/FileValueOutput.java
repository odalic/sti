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

import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomDateJsonSerializer;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.users.User;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomDateJsonDeserializer;

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

  public FileValueOutput(File adaptee) {
    owner = adaptee.getOwner();
    id = adaptee.getId();
    uploaded = adaptee.getUploaded();
    location = adaptee.getLocation();
    format = adaptee.getFormat();
    cached = adaptee.isCached();
  }

  /**
   * @return the owner
   */
  @XmlElement
  @Nullable
  public User getOwner() {
    return owner;
  }

  /**
   * @param owner the owner to set
   */
  public void setOwner(User owner) {
    Preconditions.checkNotNull(owner);

    this.owner = owner;
  }

  /**
   * @return the id
   */
  @XmlElement
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
   * @return the uploaded
   */
  @JsonSerialize(using = CustomDateJsonSerializer.class)
  @JsonDeserialize(using = CustomDateJsonDeserializer.class)
  @XmlElement
  @Nullable
  public Date getUploaded() {
    return uploaded;
  }

  /**
   * @param uploaded the uploaded to set
   */
  public void setUploaded(Date uploaded) {
    Preconditions.checkNotNull(uploaded);

    this.uploaded = uploaded;
  }

  /**
   * @return the location
   */
  @XmlElement
  @Nullable
  public URL getLocation() {
    return location;
  }

  /**
   * @param location the location to set
   */
  public void setLocation(URL location) {
    Preconditions.checkNotNull(location);

    this.location = location;
  }

  /**
   * @return the format
   */
  @XmlElement
  @Nullable
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
   * @return cached
   */
  @XmlElement
  public boolean isCached() {
    return cached;
  }

  /**
   * @param cached cached
   */
  public void setCached(boolean cached) {
    this.cached = cached;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "FileValueOutput [owner=" + owner + ", id=" + id + ", uploaded=" + uploaded
        + ", location=" + location + ", format=" + format + ", cached=" + cached + "]";
  }
}
