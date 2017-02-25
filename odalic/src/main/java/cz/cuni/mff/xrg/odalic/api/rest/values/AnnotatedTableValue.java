package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.TableContextJsonDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.TableContextJsonSerializer;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.TableContext;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.TableSchema;

/**
 * Domain class {@link AnnotatedTable} adapted for REST API.
 *
 * @author Josef Janoušek
 *
 */
@XmlRootElement(name = "annotatedTable")
@JsonInclude(Include.NON_NULL)
public final class AnnotatedTableValue implements Serializable {

  private static final long serialVersionUID = -7973901982616352L;

  private TableContext context;

  private String url;

  private TableSchema tableSchema;

  public AnnotatedTableValue() {}

  public AnnotatedTableValue(final AnnotatedTable adaptee) {
    this.context = adaptee.getContext();
    this.url = adaptee.getUrl();
    this.tableSchema = adaptee.getTableSchema();
  }

  /**
   * @return the context
   */
  @XmlElement(name = "@context")
  @JsonDeserialize(using = TableContextJsonDeserializer.class)
  @JsonSerialize(using = TableContextJsonSerializer.class)
  public TableContext getContext() {
    return this.context;
  }

  /**
   * @return the table schema
   */
  @XmlElement
  public TableSchema getTableSchema() {
    return this.tableSchema;
  }

  /**
   * @return the url
   */
  @XmlElement
  public String getUrl() {
    return this.url;
  }

  @Override
  public String toString() {
    return "AnnotatedTableValue [context=" + this.context + ", url=" + this.url + ", tableSchema="
        + this.tableSchema + "]";
  }
}
