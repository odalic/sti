package cz.cuni.mff.xrg.odalic.outputs.rdfexport.tp;

import org.eclipse.rdf4j.model.IRI;

import com.google.common.base.Preconditions;

/**
 *
 *
 * @author Josef Janoušek
 *
 */
public class ObjectListPropertyTriplePattern extends ObjectPropertyTriplePattern {

  private final String separator;

  public ObjectListPropertyTriplePattern(final String subjectPattern, final IRI predicate,
      final String objectPattern, final String separator) {
    super(subjectPattern, predicate, objectPattern, null);
    Preconditions.checkNotNull(separator, "The separator cannot be null!");
    this.separator = separator;
  }

  public String getSeparator() {
    return this.separator;
  }

}
