package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import java.util.List;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.google.common.base.Splitter;

import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import com.google.common.base.Preconditions;

/**
 * Map key JSON deserializer for {@link ColumnRelationPosition} instances.
 *
 * @author Václav Brodec
 *
 */
public final class ColumnRelationPositionKeyJsonDeserializer extends KeyDeserializer {

  @Override
  public Object deserializeKey(final String key, final DeserializationContext ctxt) {
    final List<String> indices =Splitter.on(ColumnRelationPositionKeyJsonSerializer.DELIMITER).splitToList(key);
    Preconditions.checkArgument(indices.size() == 2, "Invalid column relation position key format!");
    
    try {
      return new ColumnRelationPosition(Integer.valueOf(indices.get(0)), Integer.valueOf(indices.get(1)));
    } catch (final NumberFormatException e) {
      throw new IllegalArgumentException("Invalid column relation position key format!", e);
    }
  }
}
