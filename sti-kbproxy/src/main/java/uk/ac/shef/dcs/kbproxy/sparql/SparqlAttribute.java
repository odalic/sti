package uk.ac.shef.dcs.kbproxy.sparql;

import uk.ac.shef.dcs.kbproxy.ProxyDefinition;
import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.utils.Uris;

/**
 * Created by - on 10/06/2016.
 *
 * TODO no need for this class, alias predicates, description predicates can be stored in config.
 *
 */
public class SparqlAttribute extends Attribute {

  private static final long serialVersionUID = -1365433077663956951L;

  public SparqlAttribute(String relationLabel, String relationURI, String value, String valueURI) {
    super(relationLabel, relationURI, value, valueURI);
  }

  @Override
  public boolean isAlias(ProxyDefinition definition) {
    if (definition instanceof SparqlProxyDefinition) {
      return Uris.httpVersionAgnosticContains(((SparqlProxyDefinition)definition).getStructurePredicateLabel(), getRelationURI());
    }

    return false;
  }

  @Override
  public boolean isDescription(ProxyDefinition definition) {
    if (definition instanceof SparqlProxyDefinition) {
      return Uris.httpVersionAgnosticContains(((SparqlProxyDefinition)definition).getStructurePredicateDescription(), getRelationURI());
    }

    return false;
  }

  @Override
  public String getValueURI() {
    return valueURI;
  }

  @Override
  public void setValueURI(String valueURI) {
    this.valueURI = valueURI;
  }
}
