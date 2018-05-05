package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.common.collect.ImmutableList;

@XmlRootElement(name = "declaredEntity")
public final class DeclaredEntityValue implements Serializable {

  private static final long serialVersionUID = -9009289293200568981L;

  private URI uri;
  
  private List<String> labels;

  public DeclaredEntityValue() {
    this.uri = null;
    this.labels = ImmutableList.of();
  }
  
  public DeclaredEntityValue(final URI uri, final List<? extends String> labels) {
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
    return "DeclaredEntityValue [uri=" + uri + ", labels=" + labels + "]";
  }
}
