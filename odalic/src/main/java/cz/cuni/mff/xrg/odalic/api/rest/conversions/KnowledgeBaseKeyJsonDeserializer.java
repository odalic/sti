package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * Map key JSON deserializer for {@link KnowledgeBase} instances.
 *
 * @author Václav Brodec
 *
 */
public final class KnowledgeBaseKeyJsonDeserializer extends KeyDeserializer {

  @Override
  public Object deserializeKey(final String key, final DeserializationContext ctxt) {
    return new KnowledgeBase(key);
  }
}
