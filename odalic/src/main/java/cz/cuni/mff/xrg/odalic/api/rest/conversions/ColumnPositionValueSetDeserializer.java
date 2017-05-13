package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnPositionValue;

/**
 * A custom JSON deserializer of a set of column positions.
 *
 * @author Václav Brodec
 *
 */
public final class ColumnPositionValueSetDeserializer
    extends JsonDeserializer<Set<ColumnPositionValue>> {

  @Override
  public Set<ColumnPositionValue> deserialize(final JsonParser parser,
      final DeserializationContext ctxt) throws IOException, JsonProcessingException {
    final ColumnPositionValue[] array = ctxt.readValue(parser, ColumnPositionValue[].class);

    return ImmutableSet.copyOf(array);
  }
}
