package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.DisambiguationValue;
import cz.cuni.mff.xrg.odalic.feedbacks.Disambiguation;


public final class DisambiguationAdapter extends XmlAdapter<DisambiguationValue, Disambiguation> {

  @Override
  public DisambiguationValue marshal(final Disambiguation bound) throws Exception {
    return new DisambiguationValue(bound);
  }

  @Override
  public Disambiguation unmarshal(final DisambiguationValue value) throws Exception {
    return new Disambiguation(value.getPosition(), value.getAnnotation());
  }
}
