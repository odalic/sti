package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Serializable;
import java.net.URI;
import java.util.NavigableSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.common.collect.ImmutableSortedSet;

/**
 * ExtraRelaTable domain class adapted for REST API (and later mapped to JSON).
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "property")
public final class PropertyValue implements Serializable {

  private static final long serialVersionUID = -9009289293200568981L;

  private UUID uuid;
  
  private URI uri;
  
  private NavigableSet<String> labels;

  private PropertyValue() {
    this.uuid = null;
    this.uri = null;
    this.labels = ImmutableSortedSet.of();
  }
  
  @XmlElement
  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(final UUID uuid) {
    checkNotNull(uuid);
    
    this.uuid = uuid;
  }
  
  @XmlElement
  @Nullable
  public URI getUri() {
    return uri;
  }

  public void setUri(final URI uri) {
    this.uri = uri;
  }
  
  @XmlElement
  public NavigableSet<String> getLabels() {
    return labels;
  }

  public void setLabels(final Set<? extends String> labels) {
    checkNotNull(labels);
    
    this.labels = ImmutableSortedSet.copyOf(labels);
  }

  @Override
  public String toString() {
    return "PropertyValue [uuid=" + uuid + ", uri=" + uri + ", labels=" + labels + "]";
  }
}
