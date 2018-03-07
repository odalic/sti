package cz.cuni.mff.xrg.odalic.tasks.postprocessing;

import java.util.Collection;
import java.util.List;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;

public interface PostProcessorFactory {

	List<PostProcessor> getPostProcessors(Collection<? extends KnowledgeBase> usedBases);

}
