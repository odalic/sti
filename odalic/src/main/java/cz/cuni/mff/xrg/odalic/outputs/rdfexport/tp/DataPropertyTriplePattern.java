package cz.cuni.mff.xrg.odalic.outputs.rdfexport.tp;

import org.eclipse.rdf4j.model.IRI;

import com.google.common.base.Preconditions;

/**
 * Created by tomasknap on 18/08/16.
 *
 * @author Tomáš Knap
 *
 */
public class DataPropertyTriplePattern extends TriplePattern {

  private final String objectColumn;

  public DataPropertyTriplePattern(final String subjectPattern, final IRI predicate,
      final String objectColumn) {
    super(subjectPattern, predicate);
    Preconditions.checkNotNull(objectColumn, "The objectColumn cannot be null!");
    this.objectColumn = objectColumn;
  }

  public String getObjectColumnName() {
    return this.objectColumn;
  }

}
