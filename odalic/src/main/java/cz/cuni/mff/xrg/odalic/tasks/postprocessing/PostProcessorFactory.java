package cz.cuni.mff.xrg.odalic.tasks.postprocessing;

import java.util.Collection;
import java.util.List;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;

/**
 * Factory of {@link PostProcessor}s.
 * 
 * @author Václav Brodec
 *
 */
public interface PostProcessorFactory {

  /**
   * Compiles list of supported post-processors according to configuration of the available knowledge
   * bases.
   * 
   * @param usedBases available knowledge bases
   * @return the list of post-processors
   */
  List<PostProcessor> getPostProcessors(Collection<? extends KnowledgeBase> usedBases);

}
