package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Property type types adapter for REST API.
 *
 * @author Václav Brodec
 */
@XmlType
@XmlEnum(String.class)
@XmlRootElement(name = "propertyType")
public enum PropertyTypeValue {
  @XmlEnumValue("data") DATA,

  @XmlEnumValue("object") OBJECT
}
