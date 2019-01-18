package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;

/**
 * Map key JSON serializer for {@link ColumnRelationPosition} instances.
 *
 * @author Václav Brodec
 *
 */
public final class ColumnRelationPositionKeyJsonSerializer extends JsonSerializer<ColumnRelationPosition> {

  public static final String DELIMITER = ";";

  @Override
  public void serialize(final ColumnRelationPosition value, final JsonGenerator jgen,
      final SerializerProvider provider) throws IOException, JsonProcessingException {
    jgen.writeFieldName(value.getFirstIndex() + DELIMITER + value.getSecondIndex());
  }

}
