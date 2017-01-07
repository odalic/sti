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
  private boolean headerCaseIgnored;
  private Character quoteCharacter;
  private Character escapeCharacter;
  private Character commentMarker;


  public FormatValue(Format adaptee) {
    charset = adaptee.getCharset().name();
    delimiter = adaptee.getDelimiter();
    emptyLinesIgnored = adaptee.isEmptyLinesIgnored();
    headerCaseIgnored = adaptee.isHeaderCaseIgnored();
    quoteCharacter = adaptee.getQuoteCharacter();
    escapeCharacter = adaptee.getEscapeCharacter();
    commentMarker = adaptee.getCommentMarker();
  }


  public FormatValue() {
    this(new Format());
  }


  /**
   * @return the character set
   */
  @XmlElement
  @Nullable
  public String getCharset() {
    return charset;
  }


  /**
   * @param charset the character set to set
   */
  public void setCharset(String charset) {
    Preconditions.checkNotNull(charset);

    this.charset = charset;
  }


  /**
   * @return the delimiter
   */
  @XmlElement
  public char getDelimiter() {
    return delimiter;
  }


  /**
   * @param delimiter the delimiter to set
   */
  public void setDelimiter(char delimiter) {
    this.delimiter = delimiter;
  }


  /**
   * @return the emptyLinesIgnored
   */
  @XmlElement
  public boolean isEmptyLinesIgnored() {
    return emptyLinesIgnored;
  }


  /**
   * @param emptyLinesIgnored the emptyLinesIgnored to set
   */
  public void setEmptyLinesIgnored(boolean emptyLinesIgnored) {
    this.emptyLinesIgnored = emptyLinesIgnored;
  }


  /**
   * @return the headerCaseIgnored
   */
  @XmlElement
  public boolean isHeaderCaseIgnored() {
    return headerCaseIgnored;
  }


  /**
   * @param headerCaseIgnored the headerCaseIgnored to set
   */
  public void setHeaderCaseIgnored(boolean headerCaseIgnored) {
    this.headerCaseIgnored = headerCaseIgnored;
  }


  /**
   * @return the quoteCharacter
   */
  @XmlElement
  @Nullable
  public Character getQuoteCharacter() {
    return quoteCharacter;
  }


  /**
   * @param quoteCharacter the quoteCharacter to set
   */
  public void setQuoteCharacter(@Nullable Character quoteCharacter) {
    this.quoteCharacter = quoteCharacter;
  }


  /**
   * @return the escapeCharacter
   */
  @XmlElement
  @Nullable
  public Character getEscapeCharacter() {
    return escapeCharacter;
  }


  /**
   * @param escapeCharacter the escapeCharacter to set
   */
  public void setEscapeCharacter(@Nullable Character escapeCharacter) {
    this.escapeCharacter = escapeCharacter;
  }


  /**
   * @return the commentMarker
   */
  @XmlElement
  @Nullable
  public Character getCommentMarker() {
    return commentMarker;
  }


  /**
   * @param commentMarker the commentMarker to set
   */
  public void setCommentMarker(@Nullable Character commentMarker) {
    this.commentMarker = commentMarker;
  }


  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "FormatValue [charset=" + charset + ", delimiter=" + delimiter + ", emptyLinesIgnored="
        + emptyLinesIgnored + ", headerCaseIgnored=" + headerCaseIgnored + ", quoteCharacter="
        + quoteCharacter + ", escapeCharacter=" + escapeCharacter + ", commentMarker="
        + commentMarker + "]";
  }
}
