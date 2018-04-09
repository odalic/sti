package cz.cuni.mff.xrg.odalic.entities;

import java.util.List;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;

public interface EntitiesServicesFactory {

  List<EntitiesService> getEntitiesServices(KnowledgeBase base);

}
