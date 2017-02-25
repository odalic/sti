/**
 *
 */
package cz.cuni.mff.xrg.odalic.files.formats;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.csv.CSVFormat;

/**
 * Default {@link ApacheCsvFormatAdapter} implementation.
 *
 * @author Jan Váňa
 * @author Václav Brodec
 *
 */
@Immutable
public final class DefaultApacheCsvFormatAdapter implements ApacheCsvFormatAdapter {

  @Override
  public CSVFormat toApacheCsvFormat(final Format applicationFormat) {
    CSVFormat format = CSVFormat.newFormat(applicationFormat.getDelimiter())
        .withAllowMissingColumnNames().withIgnoreEmptyLines(applicationFormat.isEmptyLinesIgnored())
        .withRecordSeparator(applicationFormat.getLineSeparator());

    final Character quoteCharacter = applicationFormat.getQuoteCharacter();
    if (quoteCharacter != null) {
      format = format.withQuote(quoteCharacter);
    }

    format = format.withHeader(); // Must be present.

    final Character escapeCharacter = applicationFormat.getEscapeCharacter();
    if (escapeCharacter != null) {
      format = format.withEscape(escapeCharacter);
    }

    final Character commentMarker = applicationFormat.getCommentMarker();
    if (commentMarker != null) {
      format = format.withCommentMarker(commentMarker);
    }

    return format;
  }

}
