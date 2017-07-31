package uk.ac.shef.dcs.kbproxy.sparql.pp;

import uk.ac.shef.dcs.kbproxy.ProxyDefinition;
import uk.ac.shef.dcs.kbproxy.sparql.SparqlProxyDefinition;

/**
 * Changes needed:
 *
 * //private String insertEndpoint;
 * //private String insertGraph;

 */
public class PPProxyDefinition extends SparqlProxyDefinition implements ProxyDefinition {

  public static class PPBuilder extends Builder {


    private String ppServerUrl;
    private String ppProjectId;
    private String ppOntologyUrl;
    private String ppCustomSchemaUrl;
    private String ppConceptSchemaProposed;

    public void setPpServerUrl(String ppServerUrl) {
      this.ppServerUrl = ppServerUrl;
    }

    public void setPpOntologyUrl(String ppOntologyUrl) {
      this.ppOntologyUrl = ppOntologyUrl;
    }

    public void setPpCustomSchemaUrl(String ppCustomSchemaUrl) {
      this.ppCustomSchemaUrl = ppCustomSchemaUrl;
    }

    public void setPpConceptSchemaProposed(String ppConceptSchemaProposed) {
      this.ppConceptSchemaProposed = ppConceptSchemaProposed;
    }

    public void setPpProjectId(String ppProjectId) {

      this.ppProjectId = ppProjectId;
    }

    @Override
    public PPProxyDefinition build() {
      return new PPProxyDefinition(this);
    }

  }

  public static PPBuilder builder() {
    return new PPBuilder();
  }

  public static final String POOLPARTY_SERVER_URL = "ppServerUrl";
  public static final String POOLPARTY_PROJECT_ID = "ppProjectId";
  public static final String POOLPARTY_ONTOLOGY_URL = "ppOntologyUrl";
  public static final String POOLPARTY_CUSTOM_SCHEMA_URL = "ppCustomSchemaUrl";
  public static final String POOLPARTY_CONCEPT_SCHEMA_PROPOSED_URL = "ppConceptSchemaProposed";

  private final String ppServerUrl;
  private final String ppProjectId;
  private final String ppOntologyUrl;
  private final String ppCustomSchemaUrl;
  private final String ppConceptSchemaProposed;

  public String getPpProjectId() {
    return ppProjectId;
  }

  public String getPpOntologyUrl() {
    return ppOntologyUrl;
  }

  public String getPpCustomSchemaUrl() {
    return ppCustomSchemaUrl;
  }

  public String getPpConceptSchemaProposed() {
    return ppConceptSchemaProposed;
  }

  public String getPpServerUrl() {
    return ppServerUrl;
  }

  /**
   * //    this.insertEndpoint = builder.insertEndpoint;
   * //    this.insertGraph = builder.insertGraph;
   * @param builder
     */
  private PPProxyDefinition(final PPBuilder builder) {
    super(builder);

    this.ppServerUrl = builder.ppServerUrl;
    this.ppProjectId = builder.ppProjectId;
    this.ppOntologyUrl = builder.ppOntologyUrl;
    this.ppCustomSchemaUrl = builder.ppCustomSchemaUrl;
    this.ppConceptSchemaProposed = builder.ppConceptSchemaProposed;

  }
}
