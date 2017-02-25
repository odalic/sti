package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.files.formats.Format;

/**
 * Domain class {@link Format} adapted for REST API.
 *
 * @author Václav Brodec
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


  public FormatValue() {
    this(new Format());
  }


  public FormatValue(final Format adaptee) {
    this.charset = adaptee.getCharset().name();
    this.delimiter = adaptee.getDelimiter();
    this.emptyLinesIgnored = adaptee.isEmptyLinesIgnored();
    this.quoteCharacter = adaptee.getQuoteCharacter();
    this.escapeCharacter = adaptee.getEscapeCharacter();
    this.commentMarker = adaptee.getCommentMarker();
  }


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
    Preconditions.checkNotNull(charset);

    this.charset = charset;
  }


  /**
   * @param quoteCharacter the quoteCharacter to set
   */
  public void setQuoteCharacter(@Nullable final Character quoteCharacter) {
    this.quoteCharacter = quoteCharacter;
  }


  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "FormatValue [charset=" + this.charset + ", delimiter=" + this.delimiter
        + ", emptyLinesIgnored=" + this.emptyLinesIgnored + ", quoteCharacter="
        + this.quoteCharacter + ", escapeCharacter=" + this.escapeCharacter + ", commentMarker="
        + this.commentMarker + "]";
  }
}
