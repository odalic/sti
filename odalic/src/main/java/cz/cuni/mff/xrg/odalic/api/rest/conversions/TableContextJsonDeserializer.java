package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.TableContext;

/**
 * A custom JSON deserializer of a context of annotated table.
 *
 * @author Josef Janoušek
 *
 */
public final class TableContextJsonDeserializer extends JsonDeserializer<TableContext> {

  /*
   * (non-Javadoc)
   *
   * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.
   * JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
   */
  @SuppressWarnings("unchecked")
  @Override
  public TableContext deserialize(final JsonParser parser, final DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    final Object[] value = ctxt.readValue(parser, Object[].class);

    return new TableContext((Map<String, String>) value[1]);
  }
}
