package cz.cuni.mff.xrg.odalic.input;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import cz.cuni.mff.xrg.odalic.files.formats.Format;

/**
 * CSV input parser.
 * 
 * @author Václav Brodec
 *
 */
public interface CsvInputParser {
  ParsingResult parse(String csvContent, String identifier, Format configuration, int rowsLimit)
      throws IOException;

  ParsingResult parse(Reader csvReader, String identifier, Format configuration, int rowsLimit)
      throws IOException;

  ParsingResult parse(InputStream csvStream, String identifier, Format configuration, int rowsLimit)
      throws IOException;
}
