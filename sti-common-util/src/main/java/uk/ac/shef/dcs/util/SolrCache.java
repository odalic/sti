package uk.ac.shef.dcs.util;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;

import com.google.common.base.Preconditions;

/**
 * Solr cache instance.
 * 
 * @author Ziqi Zhang
 * @author Václav Brodec
 */
public final class SolrCache implements Cache {

  private static final String idFieldName = "id";
  private static final String valueFieldName = "value";
  
  private final EmbeddedSolrServer server;

  public SolrCache(final EmbeddedSolrServer server) {
    Preconditions.checkNotNull(server);
    
    this.server = server;
  }

  @Override
  public void cache(final String key, final Object value, final boolean commit) throws IOException {
    final SolrInputDocument newDoc = new SolrInputDocument();
    newDoc.addField(idFieldName, key);
    newDoc.addField(valueFieldName, SerializationUtils.serializeBase64(value));
    
    try {
      this.server.add(newDoc);
    } catch (final SolrServerException e) {
      throw new IOException("Failed to add.", e);
    }
    
    if (commit) {
      commit();
    }
  }

  @Override
  public void commit() throws IOException {
    try {
      this.server.commit();
    } catch (final SolrServerException e) {
      throw new IOException("Failed to commit.", e);
    }
  }

  public EmbeddedSolrServer getServer() {
    return this.server;
  }

  @Override
  public Object retrieve(final String key) throws IOException {
    final SolrParams params = createQueryParameters(key);

    final QueryResponse response = query(params);
    
    if (isEmpty(response)) {
      return null;
    }

    /* Following statements should be rewritten. Weird casts and so on... */
    final SolrDocument doc = response.getResults().get(0);
    if (doc.getFieldValue(valueFieldName) == null) {
      return null;
    }

    final Object data = doc.getFieldValue(valueFieldName);
    @SuppressWarnings("rawtypes") // Too important to mess with, but highly questionable piece of code!
    final Object dataBytes = ((ArrayList) data).get(0);

    final Object result;
    try {
      result = SerializationUtils.deserializeBase64((byte[]) dataBytes);
    } catch (final ClassNotFoundException e) {
      throw new IOException("Failed to deserialize the result.", e);
    }
    return result;
  }

  private boolean isEmpty(final QueryResponse response) {
    return response.getResults().getNumFound() == 0;
  }

  private QueryResponse query(final SolrParams params) throws IOException {
    final QueryResponse response;
    try {
      response = this.server.query(params);
    } catch (SolrServerException e) {
      throw new IOException("Failed to query.", e);
    }
    return response;
  }

  private static SolrParams createQueryParameters(final String key) {
    final ModifiableSolrParams result = new ModifiableSolrParams();
    result.set("q", idFieldName + ":" + ClientUtils.escapeQueryChars(key));
    result.set("fl", idFieldName + "," + valueFieldName);
    
    return result;
  }

  @Override
  public void shutdown() throws IOException {
    this.server.close();
  }

  @Override
  public String toString() {
    return "SolrCache [server=" + server + "]";
  }
}
