package cz.cuni.mff.xrg.odalic.outputs.csvexport;

import java.io.IOException;
import java.io.StringWriter;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.files.formats.ApacheCsvFormatAdapter;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.input.Input;

/**
 * Default implementation of the {@link CSVExporter}.
 *
 * @author Josef Janoušek
 */
@Immutable
public class DefaultCSVExporter implements CSVExporter {

  private final ApacheCsvFormatAdapter apacheCsvFormatAdapter;

  @Autowired
  public DefaultCSVExporter(final ApacheCsvFormatAdapter apacheCsvFormatAdapter) {
    Preconditions.checkNotNull(apacheCsvFormatAdapter, "The apacheCsvFormatAdapter cannot be null!");

    this.apacheCsvFormatAdapter = apacheCsvFormatAdapter;
  }

  @Override
  public String export(final Input content, final Format configuration) throws IOException {
    final CSVFormat format = this.apacheCsvFormatAdapter.toApacheCsvFormat(configuration);
    final StringWriter stringWriter = new StringWriter();
    final CSVPrinter csvPrinter = new CSVPrinter(stringWriter, format);

    csvPrinter.printRecord(content.headers());
    csvPrinter.printRecords(content.rows());

    csvPrinter.flush();
    csvPrinter.close();

    return stringWriter.toString().trim();
  }

}
