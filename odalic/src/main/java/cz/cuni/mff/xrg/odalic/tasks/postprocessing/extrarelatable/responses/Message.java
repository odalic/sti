package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.responses;

import java.net.URI;
import java.util.List;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.common.collect.ImmutableList;


/**
 * <p>Reporting message with extra details for developers.</p>
 * 
 * <p>Adapted from Odalic main application with permission.</p>
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement
public final class Message {

  private String text;

  private List<URI> additionalResources;

  private String debugContent;

  public Message() {
    this.text = null;
    this.additionalResources = ImmutableList.of();
    this.debugContent = null;
  }
  
  /**
   * @return the additional resources
   */
  @XmlElement
  public List<URI> getAdditionalResources() {
    return this.additionalResources;
  }

  /**
   * @return the debug content
   */
  @XmlElement
  @Nullable
  public String getDebugContent() {
    return this.debugContent;
  }

  /**
   * @return the text
   */
  @XmlElement
  @Nullable
  public String getText() {
    return this.text;
  }
  
  public void setText(String text) {
    this.text = text;
  }

  public void setAdditionalResources(List<? extends URI> additionalResources) {
    this.additionalResources = ImmutableList.copyOf(additionalResources);
  }

  public void setDebugContent(String debugContent) {
    this.debugContent = debugContent;
  }

  @Override
  public String toString() {
    return "Message [text=" + this.text + ", additionalResources=" + this.additionalResources
        + ", debugContent=" + this.debugContent + "]";
  }
}
