package cz.cuni.mff.xrg.odalic.input;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.files.formats.ApacheCsvFormatAdapter;
import cz.cuni.mff.xrg.odalic.files.formats.Format;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

/**
 * Default implementation of the {@link CsvInputParser}.
 * 
 * @author Jan Váňa
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

  @Override
  public Input parse(String content, String identifier, Format configuration) throws IOException {
    try (Reader reader = new StringReader(content)) {
      return parse(reader, identifier, configuration);
    }
  }

  @Override
  public Input parse(InputStream stream, String identifier, Format configuration)
      throws IOException {
    try (Reader reader = new InputStreamReader(stream, configuration.getCharset())) {
      return parse(reader, identifier, configuration);
    }
  }

  @Override
  public Input parse(Reader reader, String identifier, Format configuration) throws IOException {
    final CSVFormat format = this.apacheCsvFormatAdapter.toApacheCsvFormat(configuration);
    final CSVParser parser = format.parse(reader);

    inputBuilder.clear();
    inputBuilder.setFileIdentifier(identifier);
    handleHeaders(parser);

    int row = 0;
    for (CSVRecord record : parser) {
      handleInputRow(record, row);
      row++;
    }

    return inputBuilder.build();
  }

  private void handleInputRow(CSVRecord row, int rowIndex) throws IOException {
    if (!row.isConsistent()) {
      throw new IOException("CSV file is not consistent: data row with index " + rowIndex
          + " has different size than header row.");
    }

    int column = 0;
    for (String value : row) {
      inputBuilder.insertCell(value, rowIndex, column);
      column++;
    }
  }

  private void handleHeaders(CSVParser parser) {
    final Map<String, Integer> headerMap = parser.getHeaderMap();

    for (Map.Entry<String, Integer> headerEntry : headerMap.entrySet()) {
      inputBuilder.insertHeader(headerEntry.getKey(), headerEntry.getValue());
    }
  }
}
