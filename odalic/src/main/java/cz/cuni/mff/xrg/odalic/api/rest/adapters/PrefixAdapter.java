package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.PrefixValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.Prefix;


public final class PrefixAdapter extends XmlAdapter<PrefixValue, Prefix> {

  @Override
  public PrefixValue marshal(Prefix bound) throws Exception {
    return new PrefixValue(bound);
  }

  @Override
  public Prefix unmarshal(PrefixValue value) throws Exception {
    return Prefix.create(value.getWith(), value.getWhat());
  }
}
