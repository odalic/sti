package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.TableContextValue;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.TableContext;


public final class TableContextAdapter extends XmlAdapter<TableContextValue, TableContext> {

  @Override
  public TableContextValue marshal(final TableContext bound) throws Exception {
    return new TableContextValue(bound);
  }

  @Override
  public TableContext unmarshal(final TableContextValue value) throws Exception {
    return new TableContext(value.getMapping());
  }
}
