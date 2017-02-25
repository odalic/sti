package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.TableSchemaValue;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.TableSchema;


public final class TableSchemaAdapter extends XmlAdapter<TableSchemaValue, TableSchema> {

  @Override
  public TableSchemaValue marshal(final TableSchema bound) throws Exception {
    return new TableSchemaValue(bound);
  }

  @Override
  public TableSchema unmarshal(final TableSchemaValue value) throws Exception {
    return new TableSchema(value.getColumns());
  }
}
