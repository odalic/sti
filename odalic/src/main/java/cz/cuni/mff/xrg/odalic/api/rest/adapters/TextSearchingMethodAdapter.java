package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.TextSearchingMethodValue;
import cz.cuni.mff.xrg.odalic.bases.TextSearchingMethod;


public final class TextSearchingMethodAdapter extends XmlAdapter<TextSearchingMethodValue, TextSearchingMethod> {

  @Override
  public TextSearchingMethodValue marshal(final TextSearchingMethod bound) throws Exception {
    return Enum.valueOf(TextSearchingMethodValue.class, bound.name());
  }

  @Override
  public TextSearchingMethod unmarshal(final TextSearchingMethodValue value) throws Exception {
    return Enum.valueOf(TextSearchingMethod.class, value.name());
  }
}
