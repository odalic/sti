package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.TableColumn;

/**
 * Domain class {@link TableColumn} adapted for REST API.
 *
 * @author Josef Janoušek
 *
 */
@XmlRootElement(name = "tableColumn")
@JsonInclude(Include.NON_NULL)
public final class TableColumnValue implements Serializable {

  private static final long serialVersionUID = -4987474937531923887L;

  private String name;

  private List<String> titles;

  private String description;

  private String dataType;

  private Boolean virtual;

  private Boolean suppressOutput;

  private String aboutUrl;

  private String separator;

  private String propertyUrl;

  private String valueUrl;

  public TableColumnValue() {}

  public TableColumnValue(final TableColumn adaptee) {
    this.name = adaptee.getName();
    this.titles = adaptee.getTitles();
    this.description = adaptee.getDescription();
    this.dataType = adaptee.getDataType();
    this.virtual = adaptee.getVirtual();
    this.suppressOutput = adaptee.getSuppressOutput();
    this.aboutUrl = adaptee.getAboutUrl();
    this.separator = adaptee.getSeparator();
    this.propertyUrl = adaptee.getPropertyUrl();
    this.valueUrl = adaptee.getValueUrl();
  }

  /**
   * @return the aboutUrl
   */
  @XmlElement
  public String getAboutUrl() {
    return this.aboutUrl;
  }

  /**
   * @return the dataType
   */
  @XmlElement
  public String getDataType() {
    return this.dataType;
  }

  /**
   * @return the description
   */
  @XmlElement
  public String getDescription() {
    return this.description;
  }

  /**
   * @return the name
   */
  @XmlElement
  public String getName() {
    return this.name;
  }

  /**
   * @return the propertyUrl
   */
  @XmlElement
  public String getPropertyUrl() {
    return this.propertyUrl;
  }

  /**
   * @return the separator
   */
  @XmlElement
  public String getSeparator() {
    return this.separator;
  }

  /**
   * @return the suppressOutput
   */
  @XmlElement
  public Boolean getSuppressOutput() {
    return this.suppressOutput;
  }

  /**
   * @return the titles
   */
  @XmlElement
  public List<String> getTitles() {
    return this.titles;
  }

  /**
   * @return the valueUrl
   */
  @XmlElement
  public String getValueUrl() {
    return this.valueUrl;
  }

  /**
   * @return the virtual
   */
  @XmlElement
  public Boolean getVirtual() {
    return this.virtual;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "TableColumnValue [name=" + this.name + ", titles=" + this.titles + ", description="
        + this.description + ", dataType=" + this.dataType + ", virtual=" + this.virtual
        + ", suppressOutput=" + this.suppressOutput + ", aboutUrl=" + this.aboutUrl + ", separator="
        + this.separator + ", propertyUrl=" + this.propertyUrl + ", valueUrl=" + this.valueUrl
        + "]";
  }
}
