package cz.cuni.mff.xrg.odalic.outputs.csvexport;

import java.io.IOException;

import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.input.Input;

/**
 * CSV exporter.
 *
 * @author Josef Janoušek
 *
 */
public interface CSVExporter {
  /**
   * Exports Input content to CSV String.
   *
   * @param content Input content to export
   * @param configuration CSV configuration
   * @return CSV String
   * @throws IOException when an I/O error occurs 
   */
  String export(Input content, Format configuration) throws IOException;
}
