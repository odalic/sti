package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

/**
 * ExtraRelaTable domain class adapted for REST API (and later mapped to JSON).
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "format")
public final class FormatValue implements Serializable {

  private static final long serialVersionUID = -1586827772971166587L;

  private String charset;
  private char delimiter;
  private boolean emptyLinesIgnored;
  private Character quoteCharacter;
  private Character escapeCharacter;
  private Character commentMarker;


  /**
   * @return the commentMarker
   */
  @XmlElement
  @Nullable
  public Character getCommentMarker() {
    return this.commentMarker;
  }


  /**
   * @return the delimiter
   */
  @XmlElement
  public char getDelimiter() {
    return this.delimiter;
  }


  /**
   * @return the escapeCharacter
   */
  @XmlElement
  @Nullable
  public Character getEscapeCharacter() {
    return this.escapeCharacter;
  }


  /**
   * @return the character set
   */
  @XmlElement
  @Nullable
  public String getCharset() {
    return this.charset;
  }


  /**
   * @return the quoteCharacter
   */
  @XmlElement
  @Nullable
  public Character getQuoteCharacter() {
    return this.quoteCharacter;
  }


  /**
   * @return the emptyLinesIgnored
   */
  @XmlElement
  public boolean isEmptyLinesIgnored() {
    return this.emptyLinesIgnored;
  }


  /**
   * @param commentMarker the commentMarker to set
   */
  public void setCommentMarker(@Nullable final Character commentMarker) {
    this.commentMarker = commentMarker;
  }


  /**
   * @param delimiter the delimiter to set
   */
  public void setDelimiter(final char delimiter) {
    this.delimiter = delimiter;
  }


  /**
   * @param emptyLinesIgnored the emptyLinesIgnored to set
   */
  public void setEmptyLinesIgnored(final boolean emptyLinesIgnored) {
    this.emptyLinesIgnored = emptyLinesIgnored;
  }


  /**
   * @param escapeCharacter the escapeCharacter to set
   */
  public void setEscapeCharacter(@Nullable final Character escapeCharacter) {
    this.escapeCharacter = escapeCharacter;
  }


  /**
   * @param charset the character set to set
   */
  public void setCharset(final String charset) {
    Preconditions.checkNotNull(charset, "The charset cannot be null!");

    this.charset = charset;
  }


  /**
   * @param quoteCharacter the quoteCharacter to set
   */
  public void setQuoteCharacter(@Nullable final Character quoteCharacter) {
    this.quoteCharacter = quoteCharacter;
  }

  @Override
  public String toString() {
    return "FormatValue [charset=" + this.charset + ", delimiter=" + this.delimiter
        + ", emptyLinesIgnored=" + this.emptyLinesIgnored + ", quoteCharacter="
        + this.quoteCharacter + ", escapeCharacter=" + this.escapeCharacter + ", commentMarker="
        + this.commentMarker + "]";
  }
}
