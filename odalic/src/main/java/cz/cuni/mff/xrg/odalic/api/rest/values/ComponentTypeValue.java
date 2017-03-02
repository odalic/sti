package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.complexible.pinto.annotations.Iri;

/**
 * Representation of statistical component for REST API.
 *
 * @author Josef Janoušek
 *
 */
@XmlType
@XmlEnum(String.class)
@XmlRootElement(name = "component")
public enum ComponentTypeValue {
  /**
   * statistical component representing dimension
   */
  @Iri("http://odalic.eu/internal/ComponentType/dimension") @XmlEnumValue("DIMENSION") DIMENSION,

  /**
   * statistical component representing measure
   */
  @Iri("http://odalic.eu/internal/ComponentType/measure") @XmlEnumValue("MEASURE") MEASURE,

  /**
   * no statistical component
   */
  @Iri("http://odalic.eu/internal/ComponentType/none") @XmlEnumValue("NONE") NONE
}
