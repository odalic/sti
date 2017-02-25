package cz.cuni.mff.xrg.odalic.api.rest.responses;

import java.net.URI;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;


/**
 * A {@link Message} builder.
 *
 * @author Václav Brodec
 *
 */
public final class MessageBuilder {

  private String text;

  private List<URI> additionalResources;

  private String debugContent;

  public MessageBuilder() {
    this.text = null;
    this.additionalResources = ImmutableList.of();
    this.debugContent = null;
  }

  /**
   * @param additionalResources the additional resources to set
   */
  public MessageBuilder additionalResources(final List<? extends URI> additionalResources) {
    Preconditions.checkNotNull(additionalResources);

    this.additionalResources = ImmutableList.copyOf(additionalResources);

    return this;
  }

  public Message build() {
    return new Message(this.text, this.additionalResources, this.debugContent);
  }

  /**
   * @param debugContent the debug content to set
   */
  public MessageBuilder debugContent(final String debugContent) {
    this.debugContent = debugContent;

    return this;
  }

  /**
   * @return the additional resources
   */
  @Nullable
  public List<URI> getAdditionalResources() {
    return this.additionalResources;
  }

  /**
   * @return the debug content
   */
  @Nullable
  public String getDebugContent() {
    return this.debugContent;
  }

  /**
   * @return the text
   */
  @Nullable
  public String getText() {
    return this.text;
  }

  public MessageBuilder reset() {
    this.text = null;
    this.additionalResources = null;
    this.debugContent = null;

    return this;
  }

  /**
   * @param text the text to set
   */
  public MessageBuilder text(final String text) {
    this.text = text;

    return this;
  }

  @Override
  public String toString() {
    return "MessageBuilder [text=" + this.text + ", additionalResources=" + this.additionalResources
        + ", debugContent=" + this.debugContent + "]";
  }
}
