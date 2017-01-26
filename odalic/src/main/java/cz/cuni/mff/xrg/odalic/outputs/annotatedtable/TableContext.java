package cz.cuni.mff.xrg.odalic.outputs.annotatedtable;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.TableContextAdapter;

/**
 * <p>
 * This class represents a context of the annotated table.
 * </p>
 * 
 * @author Josef Janoušek
 *
 */
@Immutable
@XmlJavaTypeAdapter(TableContextAdapter.class)
public class TableContext implements Serializable {
  
  private static final long serialVersionUID = 522894844434586518L;
  
  private final String csvw = "http://www.w3.org/ns/csvw";
  
  private final Map<String, String> mapping;
  
  /**
   * Creates new annotated table context representation.
   * 
   * @param mapping mapping prefixes to URIs
   */
  public TableContext(Map<String, String> mapping) {
    Preconditions.checkNotNull(mapping);
    
    this.mapping = mapping;
  }
  
  /**
   * @return the csvw URI
   */
  public String getCsvw() {
    return csvw;
  }
  
  /**
   * @return the mapping
   */
  public Map<String, String> getMapping() {
    return mapping;
  }
  
  /**
   * Computes hash code based on all its parts.
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((csvw == null) ? 0 : csvw.hashCode());
    result = prime * result + ((mapping == null) ? 0 : mapping.hashCode());
    return result;
  }
  
  /**
   * Compares to another object for equality (only another TableContext composed from equal parts passes).
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
    TableContext other = (TableContext) obj;
    if (csvw == null) {
      if (other.csvw != null) {
        return false;
      }
    } else if (!csvw.equals(other.csvw)) {
      return false;
    }
    if (mapping == null) {
      if (other.mapping != null) {
        return false;
      }
    } else if (!mapping.equals(other.mapping)) {
      return false;
    }
    return true;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "TableContext [csvw=" + csvw + ", mapping=" + mapping + "]";
  }
}
