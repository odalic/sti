package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.TableContext;

/**
 * A custom JSON serializer of a context of annotated table.
 * 
 * @author Josef Janoušek
 *
 */
public final class TableContextJsonSerializer
    extends JsonSerializer<TableContext> {

  @Override
  public void serialize(TableContext value, JsonGenerator jgen,
      SerializerProvider provider) throws IOException, JsonProcessingException {
    jgen.writeStartArray();
    jgen.writeObject(value.getCsvw());
    jgen.writeObject(value.getMapping());
    jgen.writeEndArray();
  }

}
