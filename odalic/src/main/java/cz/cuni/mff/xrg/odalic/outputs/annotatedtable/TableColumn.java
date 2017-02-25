package cz.cuni.mff.xrg.odalic.outputs.annotatedtable;

import java.io.Serializable;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.TableColumnAdapter;

/**
 * <p>
 * This class represents a column of the annotated table.
 * </p>
 *
 * @author Josef Janoušek
 *
 */
@Immutable
@XmlJavaTypeAdapter(TableColumnAdapter.class)
public class TableColumn implements Serializable {

  private static final long serialVersionUID = 4108201271318916447L;

  private final String name;

  private final List<String> titles;

  private final String description;

  private final String dataType;

  private final Boolean virtual;

  private final Boolean suppressOutput;

  private final String aboutUrl;

  private final String separator;

  private final String propertyUrl;

  private final String valueUrl;

  /**
   * Creates new annotated table column representation.
   *
   * @param name column name
   * @param titles column header titles
   * @param description column header description
   * @param dataType column content data type
   * @param virtual column virtual
   * @param suppressOutput column with suppressed output
   * @param aboutUrl column value aboutUrl
   * @param separator column value separator
   * @param propertyUrl column value propertyUrl
   * @param valueUrl column value valueUrl
   */
  public TableColumn(final String name, final List<String> titles, final String description,
      final String dataType, final Boolean virtual, final Boolean suppressOutput,
      final String aboutUrl, final String separator, final String propertyUrl,
      final String valueUrl) {
    Preconditions.checkNotNull(name);

    this.name = name;
    this.titles = titles;
    this.description = description;
    this.dataType = dataType;
    this.virtual = virtual;
    this.suppressOutput = suppressOutput;
    this.aboutUrl = aboutUrl;
    this.separator = separator;
    this.propertyUrl = propertyUrl;
    this.valueUrl = valueUrl;
  }

  /**
   * Compares to another object for equality (only another TableColumn composed from equal parts
   * passes).
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
    final TableColumn other = (TableColumn) obj;
    if (this.name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!this.name.equals(other.name)) {
      return false;
    }
    if (this.titles == null) {
      if (other.titles != null) {
        return false;
      }
    } else if (!this.titles.equals(other.titles)) {
      return false;
    }
    if (this.description == null) {
      if (other.description != null) {
        return false;
      }
    } else if (!this.description.equals(other.description)) {
      return false;
    }
    if (this.dataType == null) {
      if (other.dataType != null) {
        return false;
      }
    } else if (!this.dataType.equals(other.dataType)) {
      return false;
    }
    if (this.virtual == null) {
      if (other.virtual != null) {
        return false;
      }
    } else if (!this.virtual.equals(other.virtual)) {
      return false;
    }
    if (this.suppressOutput == null) {
      if (other.suppressOutput != null) {
        return false;
      }
    } else if (!this.suppressOutput.equals(other.suppressOutput)) {
      return false;
    }
    if (this.aboutUrl == null) {
      if (other.aboutUrl != null) {
        return false;
      }
    } else if (!this.aboutUrl.equals(other.aboutUrl)) {
      return false;
    }
    if (this.separator == null) {
      if (other.separator != null) {
        return false;
      }
    } else if (!this.separator.equals(other.separator)) {
      return false;
    }
    if (this.propertyUrl == null) {
      if (other.propertyUrl != null) {
        return false;
      }
    } else if (!this.propertyUrl.equals(other.propertyUrl)) {
      return false;
    }
    if (this.valueUrl == null) {
      if (other.valueUrl != null) {
        return false;
      }
    } else if (!this.valueUrl.equals(other.valueUrl)) {
      return false;
    }
    return true;
  }

  /**
   * @return the aboutUrl
   */
  public String getAboutUrl() {
    return this.aboutUrl;
  }

  /**
   * @return the dataType
   */
  public String getDataType() {
    return this.dataType;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * @return the propertyUrl
   */
  public String getPropertyUrl() {
    return this.propertyUrl;
  }

  /**
   * @return the separator
   */
  public String getSeparator() {
    return this.separator;
  }

  /**
   * @return the suppressOutput
   */
  public Boolean getSuppressOutput() {
    return this.suppressOutput;
  }

  /**
   * @return the titles
   */
  public List<String> getTitles() {
    return this.titles;
  }

  /**
   * @return the valueUrl
   */
  public String getValueUrl() {
    return this.valueUrl;
  }

  /**
   * @return the virtual
   */
  public Boolean getVirtual() {
    return this.virtual;
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
    result = (prime * result) + ((this.name == null) ? 0 : this.name.hashCode());
    result = (prime * result) + ((this.titles == null) ? 0 : this.titles.hashCode());
    result = (prime * result) + ((this.description == null) ? 0 : this.description.hashCode());
    result = (prime * result) + ((this.dataType == null) ? 0 : this.dataType.hashCode());
    result = (prime * result) + ((this.virtual == null) ? 0 : this.virtual.hashCode());
    result =
        (prime * result) + ((this.suppressOutput == null) ? 0 : this.suppressOutput.hashCode());
    result = (prime * result) + ((this.aboutUrl == null) ? 0 : this.aboutUrl.hashCode());
    result = (prime * result) + ((this.separator == null) ? 0 : this.separator.hashCode());
    result = (prime * result) + ((this.propertyUrl == null) ? 0 : this.propertyUrl.hashCode());
    result = (prime * result) + ((this.valueUrl == null) ? 0 : this.valueUrl.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "TableColumn [name=" + this.name + ", titles=" + this.titles + ", description="
        + this.description + ", dataType=" + this.dataType + ", virtual=" + this.virtual
        + ", suppressOutput=" + this.suppressOutput + ", aboutUrl=" + this.aboutUrl + ", separator="
        + this.separator + ", propertyUrl=" + this.propertyUrl + ", valueUrl=" + this.valueUrl
        + "]";
  }
}
