package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.KnowledgeBaseValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;


public final class KnowledgeBaseAdapter extends XmlAdapter<KnowledgeBaseValue, KnowledgeBase> {

  @Override
  public KnowledgeBaseValue marshal(final KnowledgeBase bound) throws Exception {
    return new KnowledgeBaseValue(bound);
  }

  @Override
  public KnowledgeBase unmarshal(final KnowledgeBaseValue value) throws Exception {
    return new KnowledgeBase(value.getName());
  }
}
