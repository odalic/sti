package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Representation of column processing type for REST API.
 *
 * @author Josef Janoušek
 *
 */
@XmlType
@XmlEnum(String.class)
@XmlRootElement(name = "processingType")
public enum ColumnProcessingTypeValue {
  /**
   * processing type for column containing named entity
   */
  @XmlEnumValue("NAMED_ENTITY") NAMED_ENTITY,

  /**
   * processing type for column containing other than named entity
   */
  @XmlEnumValue("NON_NAMED_ENTITY") NON_NAMED_ENTITY,

  /**
   * processing type for ignored column
   */
  @XmlEnumValue("IGNORED") IGNORED
}
