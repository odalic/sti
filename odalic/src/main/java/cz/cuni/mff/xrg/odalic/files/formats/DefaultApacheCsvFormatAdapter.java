/**
 * 
 */
package cz.cuni.mff.xrg.odalic.files.formats;

import org.apache.commons.csv.CSVFormat;

/**
 * Default {@link ApacheCsvFormatAdapter} implementation.
 * 
 * @author Jan Váňa
 * @author Václav Brodec
 *
 */
public final class DefaultApacheCsvFormatAdapter implements ApacheCsvFormatAdapter {

  /*
   * (non-Javadoc)
   * 
   * @see
   * cz.cuni.mff.xrg.odalic.files.formats.ApacheCsvFormatAdapter#toApacheCsvFormat(cz.cuni.mff.xrg.
   * odalic.files.formats.Format)
   */
  @Override
  public CSVFormat toApacheCsvFormat(Format applicationFormat) {
    CSVFormat format = CSVFormat.newFormat(applicationFormat.getDelimiter())
        .withAllowMissingColumnNames().withIgnoreEmptyLines(applicationFormat.isEmptyLinesIgnored())
        .withIgnoreHeaderCase(applicationFormat.isHeaderCaseIgnored());

    final Character quoteCharacter = applicationFormat.getQuoteCharacter();
    if (quoteCharacter != null) {
      format = format.withQuote(quoteCharacter);
    }

    if (applicationFormat.isHeaderPresent()) {
      format = format.withHeader();
    }

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
