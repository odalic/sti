package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * Domain class {@link KnowledgeBase} adapted for REST API.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "knowledgeBase")
public final class KnowledgeBaseValue {

  private String name;

  public KnowledgeBaseValue() {}

  public KnowledgeBaseValue(final KnowledgeBase adaptee) {
    this.name = adaptee.getName();
  }

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
    Preconditions.checkNotNull(name);

    this.name = name;
  }

  @Override
  public String toString() {
    return "KnowledgeBaseValue [name=" + this.name + "]";
  }
}
