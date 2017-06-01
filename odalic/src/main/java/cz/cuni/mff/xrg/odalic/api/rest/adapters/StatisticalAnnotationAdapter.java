package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.ComponentTypeValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.StatisticalAnnotationValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.util.Annotations;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.StatisticalAnnotation;


public final class StatisticalAnnotationAdapter
    extends XmlAdapter<StatisticalAnnotationValue, StatisticalAnnotation> {

  @Override
  public StatisticalAnnotationValue marshal(final StatisticalAnnotation bound) throws Exception {
    return new StatisticalAnnotationValue(bound);
  }

  @Override
  public StatisticalAnnotation unmarshal(final StatisticalAnnotationValue value) throws Exception {
    final Map<String, ComponentTypeValue> component = value.getComponent();
    final Map<String, Set<EntityCandidate>> predicate =
        Annotations.toDomain(value.getPredicate());

    return new StatisticalAnnotation(component, predicate);
  }
}
