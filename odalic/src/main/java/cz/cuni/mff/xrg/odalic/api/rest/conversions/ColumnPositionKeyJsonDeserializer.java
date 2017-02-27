package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;

/**
 * Map key JSON deserializer for {@link ColumnPosition} instances.
 *
 * @author Václav Brodec
 *
 */
public final class ColumnPositionKeyJsonDeserializer extends KeyDeserializer {

  @Override
  public Object deserializeKey(final String key, final DeserializationContext ctxt) {
    return new ColumnPosition(Integer.parseInt(key));
  }
}
