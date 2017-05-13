package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnPositionValue;

/**
 * A custom JSON serializer of a set of column positions.
 *
 * @author Václav Brodec
 *
 */
public final class ColumnPositionValueSetSerializer
    extends JsonSerializer<Set<ColumnPositionValue>> {

  @Override
  public void serialize(final Set<ColumnPositionValue> value, final JsonGenerator jgen,
      final SerializerProvider provider) throws IOException, JsonProcessingException {
    jgen.writeStartArray();
    for (final ColumnPositionValue entry : value) {
      jgen.writeObject(entry);
    }
    jgen.writeEndArray();
  }

}
