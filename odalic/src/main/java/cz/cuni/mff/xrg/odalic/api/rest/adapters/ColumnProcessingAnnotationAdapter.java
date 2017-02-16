package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnProcessingTypeValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnProcessingAnnotationValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnProcessingAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;


public final class ColumnProcessingAnnotationAdapter
    extends XmlAdapter<ColumnProcessingAnnotationValue, ColumnProcessingAnnotation> {

  @Override
  public ColumnProcessingAnnotationValue marshal(ColumnProcessingAnnotation bound) throws Exception {
    return new ColumnProcessingAnnotationValue(bound);
  }

  @Override
  public ColumnProcessingAnnotation unmarshal(ColumnProcessingAnnotationValue value) throws Exception {
    final Map<KnowledgeBase, ColumnProcessingTypeValue> processingType = value.getProcessingType();

    return new ColumnProcessingAnnotation(processingType);
  }
}
