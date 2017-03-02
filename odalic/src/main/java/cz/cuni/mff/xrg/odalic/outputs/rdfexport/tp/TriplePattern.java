package cz.cuni.mff.xrg.odalic.outputs.rdfexport.tp;

import org.eclipse.rdf4j.model.IRI;

import com.google.common.base.Preconditions;

/**
 *
 * @author Josef Janoušek
 * @author Tomáš Knap
 *
 */
public class TriplePattern {

  private final String subjectPattern;

  private final IRI predicate;

  public TriplePattern(final String subjectPattern, final IRI predicate) {
    Preconditions.checkNotNull(subjectPattern);
    Preconditions.checkNotNull(predicate);

    this.subjectPattern = subjectPattern;
    this.predicate = predicate;
  }

  public IRI getPredicate() {
    return this.predicate;
  }

  public String getSubjectPattern() {
    return this.subjectPattern;
  }


}
