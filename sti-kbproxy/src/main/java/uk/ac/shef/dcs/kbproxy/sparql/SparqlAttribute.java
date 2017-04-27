package uk.ac.shef.dcs.kbproxy.sparql;

import uk.ac.shef.dcs.kbproxy.KnowledgeBaseProxyDefinition;
import uk.ac.shef.dcs.kbproxy.KBProxyUtils;
import uk.ac.shef.dcs.kbproxy.model.Attribute;

/**
 * Created by - on 10/06/2016.
 *
 * TODO no need for this class, alias predicates, description predicates can be stored in config.
 *
 */
public class SparqlAttribute extends Attribute {

  private static final long serialVersionUID = -1365433077663956951L;

  public SparqlAttribute(String relationURI, String value) {
    super(relationURI, value);
  }

  @Override
  public boolean isAlias(KnowledgeBaseProxyDefinition definition) {
    if (definition instanceof SparqlBaseProxyDefinition) {
      return KBProxyUtils.contains(((SparqlBaseProxyDefinition)definition).getStructurePredicateLabel(), getRelationURI());
    }

    return false;
  }

  @Override
  public boolean isDescription(KnowledgeBaseProxyDefinition definition) {
    if (definition instanceof SparqlBaseProxyDefinition) {
      return KBProxyUtils.contains(((SparqlBaseProxyDefinition)definition).getStructurePredicateDescription(), getRelationURI());
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
