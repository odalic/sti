package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.util.Map;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.ImmutableMap;

import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnProcessingAnnotation;

/**
 * <p>
 * Domain class {@link ColumnProcessingAnnotation} adapted for REST API.
 * </p>
 *
 * @author Josef Janoušek
 *
 */
@XmlRootElement(name = "columnProcessingAnnotation")
public final class ColumnProcessingAnnotationValue {

  private Map<String, ColumnProcessingTypeValue> processingType;

  public ColumnProcessingAnnotationValue() {
    this.processingType = ImmutableMap.of();
  }

  public ColumnProcessingAnnotationValue(final ColumnProcessingAnnotation adaptee) {
    this.processingType = adaptee.getProcessingType();
  }

  /**
   * @return the processing type
   */
  @XmlAnyElement
  public Map<String, ColumnProcessingTypeValue> getProcessingType() {
    return this.processingType;
  }

  /**
   * @param processingType the processing type to set
   */
  public void setProcessingType(
      final Map<? extends String, ? extends ColumnProcessingTypeValue> processingType) {
    final ImmutableMap.Builder<String, ColumnProcessingTypeValue> processingTypeBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends String, ? extends ColumnProcessingTypeValue> processingTypeEntry : processingType
        .entrySet()) {
      processingTypeBuilder.put(processingTypeEntry.getKey(), processingTypeEntry.getValue());
    }

    this.processingType = processingTypeBuilder.build();
  }

  @Override
  public String toString() {
    return "ColumnProcessingAnnotationValue [processingType=" + this.processingType + "]";
  }
}
