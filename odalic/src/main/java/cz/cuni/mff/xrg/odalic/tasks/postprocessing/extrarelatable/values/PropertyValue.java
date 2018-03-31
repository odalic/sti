package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.jena.ext.com.google.common.collect.ImmutableList;

@XmlRootElement(name = "property")
public final class PropertyValue implements Serializable {

  private static final long serialVersionUID = 8601808486386249467L;

  private URI uri;
  
  private List<String> labels;

  public PropertyValue() {
    this.uri = null;
    this.labels = ImmutableList.of();
  }
  
  public PropertyValue(final URI uri, final List<? extends String> labels) {
    this.uri = uri;
    this.setLabels(ImmutableList.copyOf(labels));
  }

  @XmlElement
  public URI getUri() {
    return uri;
  }

  public void setUri(final URI uri) {
    this.uri = uri;
  }
  
  @XmlElement
  public List<String> getLabels() {
    return labels;
  }

  public void setLabels(final List<? extends String> labels) {
    checkNotNull(labels);
    
    this.labels = ImmutableList.copyOf(labels);
  }

  @Override
  public String toString() {
    return "PropertyValue [uri=" + uri + ", labels=" + labels + "]";
  }
}
