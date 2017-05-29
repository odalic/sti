package cz.cuni.mff.xrg.odalic.outputs.rdfexport.tp;

import org.eclipse.rdf4j.model.IRI;

import com.google.common.base.Preconditions;

/**
 * Created by tomasknap on 18/08/16.
 *
 * @author Tomáš Knap
 *
 */
public class ObjectPropertyTriplePattern extends TriplePattern {

  private final String objectPattern;

  private final String dataType;

  public ObjectPropertyTriplePattern(final String subjectPattern, final IRI predicate,
      final String objectPattern, final String dataType) {
    super(subjectPattern, predicate);
    Preconditions.checkNotNull(objectPattern, "The objectPattern cannot be null!");
    this.objectPattern = objectPattern;
    this.dataType = dataType;
  }

  public String getDataType() {
    return this.dataType;
  }

  public String getObjectPattern() {
    return this.objectPattern;
  }

}
