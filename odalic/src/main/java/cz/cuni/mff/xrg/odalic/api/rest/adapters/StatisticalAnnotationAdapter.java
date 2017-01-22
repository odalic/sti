package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.ComponentTypeValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.StatisticalAnnotationValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.util.Annotations;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.StatisticalAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;


public final class StatisticalAnnotationAdapter
    extends XmlAdapter<StatisticalAnnotationValue, StatisticalAnnotation> {

  @Override
  public StatisticalAnnotationValue marshal(StatisticalAnnotation bound) throws Exception {
    return new StatisticalAnnotationValue(bound);
  }

  @Override
  public StatisticalAnnotation unmarshal(StatisticalAnnotationValue value) throws Exception {
    final Map<KnowledgeBase, ComponentTypeValue> component = value.getComponent();
    final Map<KnowledgeBase, Set<EntityCandidate>> predicate = Annotations.toDomain(value.getPredicate());

    return new StatisticalAnnotation(component, predicate);
  }
}
