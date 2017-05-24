package cz.cuni.mff.xrg.odalic.outputs.csvexport;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * Interface for {@link Result} to CSV export (extended instance of {@link Input}) conversion.
 *
 * @author Josef Janoušek
 *
 */
public interface ResultToCSVExportAdapter {

  /**
   * Converts the result to the CSV export (extended input).
   *
   * @param result Odalic result
   * @param input original input
   * @param statistical the result comes from statistical processing
   * @param primaryBase primary base
   * @return extended input for CSV export
   */
  Input toCSVExport(Result result, Input input, boolean statistical, KnowledgeBase primaryBase);
}
