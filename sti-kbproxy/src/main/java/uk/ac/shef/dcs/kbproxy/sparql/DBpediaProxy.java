package uk.ac.shef.dcs.kbproxy.sparql;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

import uk.ac.shef.dcs.kbproxy.KBDefinition;
import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.util.StringUtils;

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
                      String cachesBasePath) throws IOException, KBProxyException {
    super(kbDefinition, fuzzyKeywords, cachesBasePath);
    String ontologyURL = kbDefinition.getOntologyUri();
    if (ontologyURL != null) {
      ontology = loadModel(ontologyURL);
    }
    resultFilter = new DBpediaSearchResultFilter(kbDefinition.getStopListFile());
  }

  @Override
  protected List<String> queryForLabel(Query sparqlQuery, String resourceURI) throws KBProxyException {
    List<String> baseOut = super.queryForLabel(sparqlQuery, resourceURI);
    if (baseOut == null || baseOut.size() == 0) {
      return baseOut;
    }

    String suffix = kbDefinition.getLanguageSuffix();
    if (isNullOrEmpty(suffix)) {
      return baseOut;
    }

    List<String> out = new ArrayList<>();
    for(String label : baseOut) {
      if (label.contains("@")) { //language tag in dbpedia literals
        if (label.endsWith(suffix)) {
          label = label.substring(0, label.length() - suffix.length()).trim();
        }
        else {
          continue;
        }
      }

      out.add(label);
    }

    return out;
  }

  private OntModel loadModel(String ontURL) {
    OntModel base = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
    base.read(ontURL);
    return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, base);
  }
}
