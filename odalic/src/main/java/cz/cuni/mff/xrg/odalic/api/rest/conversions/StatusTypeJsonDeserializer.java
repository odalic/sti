package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import java.io.IOException;

import javax.ws.rs.core.Response.StatusType;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Deserializer from integer status code to.
 *
 * @author Václav Brodec
 *
 * @see CustomDateJsonSerializer
 */
public final class StatusTypeJsonDeserializer extends JsonDeserializer<StatusType> {

  @Override
  public StatusType deserialize(final JsonParser jsonparser, final DeserializationContext context)
      throws IOException, JsonProcessingException {
    throw new UnsupportedOperationException();
  }

}
