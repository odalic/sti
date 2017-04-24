package uk.ac.shef.dcs.kbproxy.sparql;

import uk.ac.shef.dcs.kbproxy.KnowledgeBaseDefinition;
import uk.ac.shef.dcs.kbproxy.KBProxyUtils;
import uk.ac.shef.dcs.kbproxy.model.Attribute;

/**
 * Created by - on 10/06/2016.
 *
 * TODO no need for this class, alias predicates, description predicates can be stored in config.
 *
 */
public class SPARQLAttribute extends Attribute {

  private static final long serialVersionUID = -1365433077663956951L;

  public SPARQLAttribute(String relationURI, String value) {
    super(relationURI, value);
  }

  @Override
  public boolean isAlias(KnowledgeBaseDefinition definition) {
    if (definition instanceof SPARQLDefinition) {
      return KBProxyUtils.contains(((SPARQLDefinition)definition).getStructurePredicateLabel(), getRelationURI());
    }

    return false;
  }

  @Override
  public boolean isDescription(KnowledgeBaseDefinition definition) {
    if (definition instanceof SPARQLDefinition) {
      return KBProxyUtils.contains(((SPARQLDefinition)definition).getStructurePredicateDescription(), getRelationURI());
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
