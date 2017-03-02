package cz.cuni.mff.xrg.odalic.input;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.files.formats.ApacheCsvFormatAdapter;
import cz.cuni.mff.xrg.odalic.files.formats.Format;

/**
 * Default implementation of the {@link CsvInputParser}.
 *
 * @author Jan Váňa
 * @author Josef Janoušek
 */
public final class DefaultCsvInputParser implements CsvInputParser {

  private final ListsBackedInputBuilder inputBuilder;
  private final ApacheCsvFormatAdapter apacheCsvFormatAdapter;

  @Autowired
  public DefaultCsvInputParser(final ListsBackedInputBuilder inputBuilder,
      final ApacheCsvFormatAdapter apacheCsvFormatAdapter) {
    Preconditions.checkNotNull(inputBuilder);
    Preconditions.checkNotNull(apacheCsvFormatAdapter);

    this.inputBuilder = inputBuilder;
    this.apacheCsvFormatAdapter = apacheCsvFormatAdapter;
  }

  private String detectSeparator(final Reader reader) throws IOException {
    try {
      reader.reset();
    } catch (final IOException e) {
      // maybe reader does not support reset(), but that could not matter
    }
    int c = reader.read();
    while (c > -1) {
      if (c == 10) {
        // Unix-style separator found
        return "\n";
      } else if (c == 13) {
        final int c2 = reader.read();
        if (c2 == 10) {
          // Windows-style separator found
          return "\r\n";
        } else {
          // Older Mac-style separator found
          return "\r";
        }
      }
      c = reader.read();
    }
    // no separator found
    return System.lineSeparator();
  }

  private void handleHeaders(final CSVParser parser) {
    final Map<String, Integer> headerMap = parser.getHeaderMap();

    for (final Map.Entry<String, Integer> headerEntry : headerMap.entrySet()) {
      this.inputBuilder.insertHeader(headerEntry.getKey(), headerEntry.getValue());
    }
  }

  private void handleInputRow(final CSVRecord row, final int rowIndex) throws IOException {
    if (!row.isConsistent()) {
      throw new IOException("CSV file is not consistent: data row with index " + rowIndex
          + " has different size than header row.");
    }

    int column = 0;
    for (final String value : row) {
      this.inputBuilder.insertCell(value, rowIndex, column);
      column++;
    }
  }

  @Override
  public ParsingResult parse(final InputStream stream, final String identifier,
      final Format configuration, final int rowsLimit) throws IOException {
    try (Reader reader = new InputStreamReader(stream, configuration.getCharset())) {
      return parse(reader, identifier, configuration, rowsLimit);
    }
  }

  @Override
  public ParsingResult parse(final Reader reader, final String identifier,
      final Format configuration, final int rowsLimit) throws IOException {
    Preconditions.checkArgument(rowsLimit > 0, "Rows limit must be a positive number.");

    final CSVFormat format = this.apacheCsvFormatAdapter.toApacheCsvFormat(configuration);
    final CSVParser parser = format.parse(reader);

    this.inputBuilder.clear();
    this.inputBuilder.setFileIdentifier(identifier);
    handleHeaders(parser);

    int row = 0;
    for (final CSVRecord record : parser) {
      if (row >= rowsLimit) {
        break;
      }

      handleInputRow(record, row);
      row++;
    }

    if (row == 0) {
      throw new IOException("There are no data rows in the CSV file.");
    }

    return new ParsingResult(this.inputBuilder.build(),
        new Format(configuration.getCharset(), configuration.getDelimiter(),
            configuration.isEmptyLinesIgnored(), configuration.getQuoteCharacter(),
            configuration.getEscapeCharacter(), configuration.getCommentMarker(),
            detectSeparator(reader)));
  }

  @Override
  public ParsingResult parse(final String content, final String identifier,
      final Format configuration, final int rowsLimit) throws IOException {
    try (Reader reader = new StringReader(content)) {
      return parse(reader, identifier, configuration, rowsLimit);
    }
  }
}
