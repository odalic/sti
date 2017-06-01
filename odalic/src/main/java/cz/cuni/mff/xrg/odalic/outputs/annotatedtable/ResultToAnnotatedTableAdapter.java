package cz.cuni.mff.xrg.odalic.outputs.annotatedtable;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * Interface for {@link Result} to {@link AnnotatedTable} conversion.
 *
 * @author Josef Janoušek
 *
 */
public interface ResultToAnnotatedTableAdapter {

  /**
   * Converts the result to the annotated table.
   *
   * @param result Odalic result
   * @param input original input
   * @param statistical the result comes from statistical processing
   * @param primaryBase primary base
   * @return annotated table
   */
  AnnotatedTable toAnnotatedTable(Result result, Input input, boolean statistical,
      KnowledgeBase primaryBase);
}
