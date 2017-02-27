package uk.ac.shef.dcs.kbproxy.sparql;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import uk.ac.shef.dcs.kbproxy.KBDefinition;
import uk.ac.shef.dcs.kbproxy.KBProxyException;
import java.io.IOException;
import java.util.*;

/**
 * Created by - on 10/06/2016.
 */
public class DBpediaProxy extends SPARQLProxy {

  private static final String DBP_SPARQL_ENDPOINT = "dbp.sparql.endpoint";
  private static final String DBP_ONTOLOGY_URL = "dbp.ontology.url";

  private OntModel ontology;

  /**
   * @param fuzzyKeywords   given a query string, kbproxy will firstly try to fetch results matching the exact query. when no match is
   *                        found, you can set fuzzyKeywords to true, to let kbproxy to break the query string based on conjunective words.
   *                        So if the query string is "tom and jerry", it will try "tom" and "jerry"
   * @param cachesBasePath  Base path for the initialized solr caches.
   * @throws IOException
   */
  public DBpediaProxy(KBDefinition kbDefinition,
                      Boolean fuzzyKeywords,
                      String cachesBasePath,
                      Map<String, String> prefixToUriMap) throws IOException, KBProxyException {
    super(kbDefinition, fuzzyKeywords, cachesBasePath, prefixToUriMap);
    String ontologyURL = kbDefinition.getOntologyUri();
    if (ontologyURL != null) {
      ontology = loadModel(ontologyURL);
    }
    resultFilter = new DBpediaSearchResultFilter(kbDefinition.getStopListFile());
  }

  @Override
  protected String applyCustomUriHeuristics(String resourceURI, String label) {
    //This is an yago resource, which may have numbered ids as suffix
    //e.g., City015467.
    if (resourceURI.contains("yago")) {
      int end = 0;
      for (int i = 0; i < label.length(); i++) {
        if (Character.isDigit(label.charAt(i))) {
          end = i;
          break;
        }
      }

      if (end > 0) {
        label = label.substring(0, end);
      }
    }

    return label;
  }

  private OntModel loadModel(String ontURL) {
    OntModel base = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
    base.read(ontURL);
    return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, base);
  }
}
