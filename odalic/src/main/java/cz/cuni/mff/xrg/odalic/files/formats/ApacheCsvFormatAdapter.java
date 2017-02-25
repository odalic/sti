package cz.cuni.mff.xrg.odalic.files.formats;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

/**
 * Adapter from {@link Format} to {@link CSVFormat}.
 *
 * @author Václav Brodec
 *
 */
public interface ApacheCsvFormatAdapter {
  /**
   * Converts to the CSV file formatting configuration used by {@link CSVParser} and
   * {@link CSVPrinter}.
   *
   * @param applicationFormat application CSV format
   * @return a {@link CSVFormat} instance derived from the application format
   */
  CSVFormat toApacheCsvFormat(Format applicationFormat);
}
