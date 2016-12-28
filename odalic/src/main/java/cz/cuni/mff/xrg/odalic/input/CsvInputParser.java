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
  Input parse(String csvContent, String identifier, Format configuration, int rowsLimit)
      throws IOException;

  Input parse(Reader csvReader, String identifier, Format configuration, int rowsLimit)
      throws IOException;

  Input parse(InputStream csvStream, String identifier, Format configuration, int rowsLimit)
      throws IOException;
}
