package cz.cuni.mff.xrg.odalic.entities.postprocessing.extrarelatable.values;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.common.collect.ImmutableList;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.PropertyValue;

/**
 * Domain class of ExtraRelaTable adapted for ERT REST API.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "searchResult")
public final class SearchResultValue {
  private List<PropertyValue> properties;

  public SearchResultValue() {
    this.properties = ImmutableList.of();
  }
  
  @XmlElement
  public List<PropertyValue> getProperties() {
    return properties;
  }

  public void setProperties(List<? extends PropertyValue> properties) {
    checkNotNull(properties);
    
    this.properties = ImmutableList.copyOf(properties);
  }

  @Override
  public String toString() {
    return "SearchResultValue [properties=" + properties + "]";
  }
}
