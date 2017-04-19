package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.HeaderAnnotationValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.util.Annotations;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;


public final class HeaderAnnotationAdapter
    extends XmlAdapter<HeaderAnnotationValue, HeaderAnnotation> {

  @Override
  public HeaderAnnotationValue marshal(final HeaderAnnotation bound) throws Exception {
    return new HeaderAnnotationValue(bound);
  }

  @Override
  public HeaderAnnotation unmarshal(final HeaderAnnotationValue value) throws Exception {
    final Map<String, NavigableSet<EntityCandidate>> candidates =
        Annotations.toNavigableDomain(value.getCandidates());
    final Map<String, Set<EntityCandidate>> chosen = Annotations.toDomain(value.getChosen());

    return new HeaderAnnotation(candidates, chosen);
  }
}
