package cz.cuni.mff.xrg.odalic.files.formats;

import java.io.Serializable;
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
public final class Format implements Serializable {

  private static final char DEFAULT_QUOTE_CHARACTER = '"';

  private static final char DEFAULT_DELIMITER = ',';

  private static final long serialVersionUID = -1910540987387436314L;

  private final String charset;
  private final char delimiter;
  private final boolean emptyLinesIgnored;
  private final Character quoteCharacter;
  private final Character escapeCharacter;
  private final Character commentMarker;
  private final String lineSeparator;


  /**
   * Creates a default format, which assumes UTF-8 character set, semicolon delimiters, the header
   * to be present and the system line separator.
   */
  public Format() {
    this.charset = StandardCharsets.UTF_8.name();
    this.delimiter = DEFAULT_DELIMITER;
    this.emptyLinesIgnored = true;
    this.quoteCharacter = DEFAULT_QUOTE_CHARACTER;
    this.escapeCharacter = null;
    this.commentMarker = null;
    this.lineSeparator = System.lineSeparator();
  }


  /**
   * Creates a new format instance with the system line separator.
   *
   * @param charset character set
   * @param delimiter fields delimiter
   * @param emptyLinesIgnored ignore empty lines
   * @param quoteCharacter use this quote character
   * @param escapeCharacter use this escaping character
   * @param commentMarker use this comment marker
   */
  public Format(final Charset charset, final char delimiter, final boolean emptyLinesIgnored,
      final @Nullable Character quoteCharacter, final @Nullable Character escapeCharacter,
      final @Nullable Character commentMarker) {
    this(charset, delimiter, emptyLinesIgnored, quoteCharacter, escapeCharacter, commentMarker,
        System.lineSeparator());
  }


  /**
   * Creates a new format instance.
   *
   * @param charset character set
   * @param delimiter fields delimiter
   * @param emptyLinesIgnored ignore empty lines
   * @param quoteCharacter use this quote character
   * @param escapeCharacter use this escaping character
   * @param commentMarker use this comment marker
   * @param lineSeparator use this line separator for generating new CSV file
   */
  public Format(final Charset charset, final char delimiter, final boolean emptyLinesIgnored,
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

    this.charset = charset.name();
    this.delimiter = delimiter;
    this.emptyLinesIgnored = emptyLinesIgnored;
    this.quoteCharacter = quoteCharacter;
    this.escapeCharacter = escapeCharacter;
    this.commentMarker = commentMarker;
    this.lineSeparator = lineSeparator;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Format other = (Format) obj;
    if (this.charset == null) {
      if (other.charset != null) {
        return false;
      }
    } else if (!this.charset.equals(other.charset)) {
      return false;
    }
    if (this.commentMarker == null) {
      if (other.commentMarker != null) {
        return false;
      }
    } else if (!this.commentMarker.equals(other.commentMarker)) {
      return false;
    }
    if (this.delimiter != other.delimiter) {
      return false;
    }
    if (this.emptyLinesIgnored != other.emptyLinesIgnored) {
      return false;
    }
    if (this.escapeCharacter == null) {
      if (other.escapeCharacter != null) {
        return false;
      }
    } else if (!this.escapeCharacter.equals(other.escapeCharacter)) {
      return false;
    }
    if (this.quoteCharacter == null) {
      if (other.quoteCharacter != null) {
        return false;
      }
    } else if (!this.quoteCharacter.equals(other.quoteCharacter)) {
      return false;
    }
    if (this.lineSeparator == null) {
      if (other.lineSeparator != null) {
        return false;
      }
    } else if (!this.lineSeparator.equals(other.lineSeparator)) {
      return false;
    }
    return true;
  }


  /**
   * @return the comment marker
   */
  @Nullable
  public Character getCommentMarker() {
    return this.commentMarker;
  }


  /**
   * @return the delimiter
   */
  public char getDelimiter() {
    return this.delimiter;
  }


  /**
   * @return the escape character
   */
  @Nullable
  public Character getEscapeCharacter() {
    return this.escapeCharacter;
  }


  /**
   * @return the character set
   */
  public Charset getCharset() {
    return Charset.forName(this.charset);
  }


  /**
   * @return the line separator
   */
  public String getLineSeparator() {
    return this.lineSeparator;
  }


  /**
   * @return the quote character
   */
  @Nullable
  public Character getQuoteCharacter() {
    return this.quoteCharacter;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.charset == null) ? 0 : this.charset.hashCode());
    result = (prime * result) + ((this.commentMarker == null) ? 0 : this.commentMarker.hashCode());
    result = (prime * result) + this.delimiter;
    result = (prime * result) + (this.emptyLinesIgnored ? 1231 : 1237);
    result =
        (prime * result) + ((this.escapeCharacter == null) ? 0 : this.escapeCharacter.hashCode());
    result =
        (prime * result) + ((this.quoteCharacter == null) ? 0 : this.quoteCharacter.hashCode());
    result = (prime * result) + ((this.lineSeparator == null) ? 0 : this.lineSeparator.hashCode());
    return result;
  }


  /**
   * @return the empty lines ignored
   */
  public boolean isEmptyLinesIgnored() {
    return this.emptyLinesIgnored;
  }

  @Override
  public String toString() {
    return "CsvConfiguration [charset=" + this.charset + ", delimiter=" + this.delimiter
        + ", emptyLinesIgnored=" + this.emptyLinesIgnored + ", quoteCharacter="
        + this.quoteCharacter + ", escapeCharacter=" + this.escapeCharacter + ", commentMarker="
        + this.commentMarker + ", lineSeparator=" + this.lineSeparator + "]";
  }
}
