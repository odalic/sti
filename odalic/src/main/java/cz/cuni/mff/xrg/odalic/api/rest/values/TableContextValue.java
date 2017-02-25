package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.TableContext;

/**
 * Domain class {@link TableContext} adapted for REST API.
 *
 * @author Josef Janoušek
 *
 */
@XmlRootElement(name = "tableContext")
@JsonInclude(Include.NON_NULL)
public final class TableContextValue implements Serializable {

  private static final long serialVersionUID = 6093586476544509692L;

  private final String csvw = "http://www.w3.org/ns/csvw";

  private Map<String, String> mapping;

  public TableContextValue() {}

  public TableContextValue(final TableContext adaptee) {
    this.mapping = adaptee.getMapping();
  }

  /**
   * @return the csvw URI
   */
  @XmlElement
  public String getCsvw() {
    return this.csvw;
  }

  /**
   * @return the mapping
   */
  @XmlElement
  public Map<String, String> getMapping() {
    return this.mapping;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "TableContextValue [csvw=" + this.csvw + ", mapping=" + this.mapping + "]";
  }
}
