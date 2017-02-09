package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.net.URL;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.formats.Format;

/**
 * Domain class {@link File} adapted for REST API input.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "file")
public final class FileValueInput implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  private URL location;
  
  private Format format; 

  public FileValueInput() {}
  
  public FileValueInput(File adaptee) {
    location = adaptee.getLocation();
    format = adaptee.getFormat();
  }

  /**
   * @return the location
   */
  @Nullable
  @XmlElement
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
  @Nullable
  @XmlElement
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

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "FileValueInput [location=" + location + ", format=" + format
        + "]";
  }
}
