package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Domain class {@link BackgroundKnowledgeGraph} adapted for REST API.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "graph")
public final class GraphValue implements Serializable {

  private static final long serialVersionUID = 1610346823333685091L;

  private String name;

  public GraphValue() {}

  /**
   * @return the name
   */
  @XmlElement
  @Nullable
  public String getName() {
    return this.name;
  }

  /**
   * @param name the name to set
   */
  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "GraphValue [name=" + this.name + "]";
  }
}
