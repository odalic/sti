package cz.cuni.mff.xrg.odalic.files.formats;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.FormatAdapter;

/**
 * Format of the CSV file.
 * 
 * @author Jan Váňa
 * @author Václav Brodec
 * @author Josef Janoušek
 */
@Immutable
@XmlJavaTypeAdapter(value = FormatAdapter.class)
public final class Format {

  private final Charset charset;
  private final char delimiter;
  private final boolean headerPresent;
  private final boolean emptyLinesIgnored;
  private final boolean headerCaseIgnored;
  private final Character quoteCharacter;
  private final Character escapeCharacter;
  private final Character commentMarker;

  private final String lineSeparator;


  /**
   * Creates a new format instance.
   * 
   * @param charset character set
   * @param delimiter fields delimiter
   * @param headerPresent header is present
   * @param emptyLinesIgnored ignore empty lines
   * @param headerCaseIgnored ignore header case
   * @param quoteCharacter use this quote character
   * @param escapeCharacter use this escaping character
   * @param commentMarker use this comment marker
   * @param lineSeparator use this line separator for generating new CSV file
   */
  public Format(final Charset charset, final char delimiter, final boolean headerPresent,
      final boolean emptyLinesIgnored, final boolean headerCaseIgnored,
      final @Nullable Character quoteCharacter, final @Nullable Character escapeCharacter,
      final @Nullable Character commentMarker, final String lineSeparator) {
    Preconditions.checkNotNull(charset);
    Preconditions.checkNotNull(lineSeparator);

    Preconditions.checkArgument(!cz.cuni.mff.xrg.odalic.util.Characters.isLineBreak(delimiter),
        "The delimiter is a line break character.");
    Preconditions.checkArgument(!cz.cuni.mff.xrg.odalic.util.Characters.isLineBreak(quoteCharacter),
        "The quote character is a line break character.");
    Preconditions.checkArgument(
        !cz.cuni.mff.xrg.odalic.util.Characters.isLineBreak(escapeCharacter),
        "The escape character is a line break character.");
    Preconditions.checkArgument(!cz.cuni.mff.xrg.odalic.util.Characters.isLineBreak(commentMarker),
        "The comment marker is a line break character.");

    this.charset = charset;
    this.delimiter = delimiter;
    this.headerPresent = headerPresent;
    this.emptyLinesIgnored = emptyLinesIgnored;
    this.headerCaseIgnored = headerCaseIgnored;
    this.quoteCharacter = quoteCharacter;
    this.escapeCharacter = escapeCharacter;
    this.commentMarker = commentMarker;
    this.lineSeparator = lineSeparator;
  }


  /**
   * Creates a new format instance with the system line separator.
   * 
   * @param charset character set
   * @param delimiter fields delimiter
   * @param headerPresent header is present
   * @param emptyLinesIgnored ignore empty lines
   * @param headerCaseIgnored ignore header case
   * @param quoteCharacter use this quote character
   * @param escapeCharacter use this escaping character
   * @param commentMarker use this comment marker
   */
  public Format(final Charset charset, final char delimiter, final boolean headerPresent,
      final boolean emptyLinesIgnored, final boolean headerCaseIgnored,
      final @Nullable Character quoteCharacter, final @Nullable Character escapeCharacter,
      final @Nullable Character commentMarker) {
    Preconditions.checkNotNull(charset);

    Preconditions.checkArgument(!cz.cuni.mff.xrg.odalic.util.Characters.isLineBreak(delimiter),
        "The delimiter is a line break character.");
    Preconditions.checkArgument(!cz.cuni.mff.xrg.odalic.util.Characters.isLineBreak(quoteCharacter),
        "The quote character is a line break character.");
    Preconditions.checkArgument(
        !cz.cuni.mff.xrg.odalic.util.Characters.isLineBreak(escapeCharacter),
        "The escape character is a line break character.");
    Preconditions.checkArgument(!cz.cuni.mff.xrg.odalic.util.Characters.isLineBreak(commentMarker),
        "The comment marker is a line break character.");

    this.charset = charset;
    this.delimiter = delimiter;
    this.headerPresent = headerPresent;
    this.emptyLinesIgnored = emptyLinesIgnored;
    this.headerCaseIgnored = headerCaseIgnored;
    this.quoteCharacter = quoteCharacter;
    this.escapeCharacter = escapeCharacter;
    this.commentMarker = commentMarker;
    this.lineSeparator = System.lineSeparator();
  }


  /**
   * Creates a default format, which assumes UTF-8 character set, semicolon delimiters,
   * the header to be present and the system line separator.
   */
  public Format() {
    charset = StandardCharsets.UTF_8;
    delimiter = ';';
    headerPresent = true;
    emptyLinesIgnored = true;
    headerCaseIgnored = false;
    quoteCharacter = null;
    escapeCharacter = null;
    commentMarker = null;
    lineSeparator = System.lineSeparator();
  }


  /**
   * @return the character set
   */
  public Charset getCharset() {
    return charset;
  }


  /**
   * @return the delimiter
   */
  public char getDelimiter() {
    return delimiter;
  }


  /**
   * @return the header present
   */
  public boolean isHeaderPresent() {
    return headerPresent;
  }


  /**
   * @return the empty lines ignored
   */
  public boolean isEmptyLinesIgnored() {
    return emptyLinesIgnored;
  }


  /**
   * @return the header case ignored
   */
  public boolean isHeaderCaseIgnored() {
    return headerCaseIgnored;
  }


  /**
   * @return the quote character
   */
  @Nullable
  public Character getQuoteCharacter() {
    return quoteCharacter;
  }


  /**
   * @return the escape character
   */
  @Nullable
  public Character getEscapeCharacter() {
    return escapeCharacter;
  }


  /**
   * @return the comment marker
   */
  @Nullable
  public Character getCommentMarker() {
    return commentMarker;
  }


  /**
   * @return the line separator
   */
  public String getLineSeparator() {
    return lineSeparator;
  }


  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((charset == null) ? 0 : charset.hashCode());
    result = prime * result + ((commentMarker == null) ? 0 : commentMarker.hashCode());
    result = prime * result + delimiter;
    result = prime * result + (emptyLinesIgnored ? 1231 : 1237);
    result = prime * result + ((escapeCharacter == null) ? 0 : escapeCharacter.hashCode());
    result = prime * result + (headerCaseIgnored ? 1231 : 1237);
    result = prime * result + (headerPresent ? 1231 : 1237);
    result = prime * result + ((quoteCharacter == null) ? 0 : quoteCharacter.hashCode());
    result = prime * result + ((lineSeparator == null) ? 0 : lineSeparator.hashCode());
    return result;
  }


  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Format other = (Format) obj;
    if (charset == null) {
      if (other.charset != null) {
        return false;
      }
    } else if (!charset.equals(other.charset)) {
      return false;
    }
    if (commentMarker == null) {
      if (other.commentMarker != null) {
        return false;
      }
    } else if (!commentMarker.equals(other.commentMarker)) {
      return false;
    }
    if (delimiter != other.delimiter) {
      return false;
    }
    if (emptyLinesIgnored != other.emptyLinesIgnored) {
      return false;
    }
    if (escapeCharacter == null) {
      if (other.escapeCharacter != null) {
        return false;
      }
    } else if (!escapeCharacter.equals(other.escapeCharacter)) {
      return false;
    }
    if (headerCaseIgnored != other.headerCaseIgnored) {
      return false;
    }
    if (headerPresent != other.headerPresent) {
      return false;
    }
    if (quoteCharacter == null) {
      if (other.quoteCharacter != null) {
        return false;
      }
    } else if (!quoteCharacter.equals(other.quoteCharacter)) {
      return false;
    }
    if (lineSeparator == null) {
      if (other.lineSeparator != null) {
        return false;
      }
    } else if (!lineSeparator.equals(other.lineSeparator)) {
      return false;
    }
    return true;
  }


  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CsvConfiguration [charset=" + charset + ", delimiter=" + delimiter + ", headerPresent="
        + headerPresent + ", emptyLinesIgnored=" + emptyLinesIgnored + ", headerCaseIgnored="
        + headerCaseIgnored + ", quoteCharacter=" + quoteCharacter + ", escapeCharacter="
        + escapeCharacter + ", commentMarker=" + commentMarker + ", lineSeparator=" + lineSeparator +"]";
  }
}
