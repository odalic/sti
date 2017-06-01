package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnProcessingAnnotationValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnProcessingTypeValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnProcessingAnnotation;


public final class ColumnProcessingAnnotationAdapter
    extends XmlAdapter<ColumnProcessingAnnotationValue, ColumnProcessingAnnotation> {

  @Override
  public ColumnProcessingAnnotationValue marshal(final ColumnProcessingAnnotation bound)
      throws Exception {
    return new ColumnProcessingAnnotationValue(bound);
  }

  @Override
  public ColumnProcessingAnnotation unmarshal(final ColumnProcessingAnnotationValue value)
      throws Exception {
    final Map<String, ColumnProcessingTypeValue> processingType = value.getProcessingType();

    return new ColumnProcessingAnnotation(processingType);
  }
}
