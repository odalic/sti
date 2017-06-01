package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Text searching method type adapted for REST API.
 *
 * @author Václav Brodec
 */
@XmlType
@XmlEnum(String.class)
@XmlRootElement(name = "textSearchingMethod")
public enum TextSearchingMethodValue {
  @XmlEnumValue("exact") EXACT,

  @XmlEnumValue("fulltext") FULLTEXT,

  @XmlEnumValue("substring") SUBSTRING
}
