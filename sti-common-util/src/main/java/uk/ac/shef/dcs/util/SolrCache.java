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

/**
 * Created with IntelliJ IDEA. User: zqz Date: 21/01/14 Time: 12:58 To change this template use File
 * | Settings | File Templates.
 */
public class SolrCache {

  private static final String idFieldName = "id";
  private static final String valueFieldName = "value";
  private final EmbeddedSolrServer server;

  public SolrCache(final EmbeddedSolrServer server) {
    this.server = server;
  }

  public void cache(final String queryId, final Object obj, final boolean commit)
      throws IOException, SolrServerException {
    final SolrInputDocument newDoc = new SolrInputDocument();
    newDoc.addField(idFieldName, queryId);
    newDoc.addField(valueFieldName, SerializationUtils.serializeBase64(obj));
    this.server.add(newDoc);
    if (commit) {
      this.server.commit();
    }
  }

  public void commit() throws IOException, SolrServerException {
    this.server.commit();
  }

  public EmbeddedSolrServer getServer() {
    return this.server;
  }

  /**
   * @param queryId
   * @return null if no cache has been created for this queryId; an empty List object if there are
   *         no results for the queryId (i.e., the query has been executed before but no results
   *         were found to match the query);
   */
  public Object retrieve(final String queryId)
      throws SolrServerException, ClassNotFoundException, IOException {
    final ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("q", idFieldName + ":" + ClientUtils.escapeQueryChars(queryId));
    params.set("fl", idFieldName + "," + valueFieldName);

    final QueryResponse response = this.server.query(params);
    if (response.getResults().getNumFound() == 0) {
      return null;
    }

    final SolrDocument doc = response.getResults().get(0);
    if (doc.getFieldValue(valueFieldName) == null) {
      return null;
    }

    final Object data = doc.getFieldValue(valueFieldName);
    @SuppressWarnings("rawtypes") // Too important to mess with.
    final Object dataBytes = ((ArrayList) data).get(0);

    final Object object = SerializationUtils.deserializeBase64((byte[]) dataBytes);
    return object;
  }

  public void shutdown() throws IOException {
    this.server.close();
  }

}
