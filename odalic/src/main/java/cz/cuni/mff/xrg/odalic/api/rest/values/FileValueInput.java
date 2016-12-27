package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.net.URL;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.input.CsvConfiguration;

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
  
  private CsvConfiguration parsingConfiguration; 

  public FileValueInput() {}
  
  public FileValueInput(File adaptee) {
    location = adaptee.getLocation();
    parsingConfiguration = adaptee.getParsingConfiguration();
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
   * @return the parser configuration
   */
  @Nullable
  @XmlElement
  public CsvConfiguration getParsingConfiguration() {
    return parsingConfiguration;
  }

  /**
   * @param parsingConfiguration the parser configuration to set
   */
  public void setParsingConfiguration(CsvConfiguration parsingConfiguration) {
    Preconditions.checkNotNull(parsingConfiguration);
    
    this.parsingConfiguration = parsingConfiguration;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "FileValueInput [location=" + location + ", parsingConfiguration=" + parsingConfiguration
        + "]";
  }
}
