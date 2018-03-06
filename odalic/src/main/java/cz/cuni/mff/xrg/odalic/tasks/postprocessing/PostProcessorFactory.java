package cz.cuni.mff.xrg.odalic.tasks.postprocessing;

import java.util.Map;
import java.util.Set;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;

public interface PostProcessorFactory {

	Map<String, PostProcessor> getPostProcessors(String userId, Set<? extends KnowledgeBase> usedBases);

}
