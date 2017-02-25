package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.FormatValue;
import cz.cuni.mff.xrg.odalic.files.formats.Format;


public final class FormatAdapter extends XmlAdapter<FormatValue, Format> {

  @Override
  public FormatValue marshal(final Format bound) throws Exception {
    return new FormatValue(bound);
  }

  @Override
  public Format unmarshal(final FormatValue value) throws Exception {
    throw new UnsupportedOperationException();
  }
}
