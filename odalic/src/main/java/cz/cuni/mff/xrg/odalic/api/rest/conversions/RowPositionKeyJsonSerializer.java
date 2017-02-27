package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import cz.cuni.mff.xrg.odalic.positions.RowPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * Map key JSON serializer for {@link KnowledgeBase} instances.
 *
 * @author Václav Brodec
 *
 */
public final class RowPositionKeyJsonSerializer extends JsonSerializer<RowPosition> {

  @Override
  public void serialize(final RowPosition value, final JsonGenerator jgen,
      final SerializerProvider provider) throws IOException, JsonProcessingException {
    jgen.writeFieldName(Integer.toString(value.getIndex()));
  }

}
