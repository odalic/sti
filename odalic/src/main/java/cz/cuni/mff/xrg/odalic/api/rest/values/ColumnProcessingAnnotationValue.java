package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.util.Map;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.google.common.collect.ImmutableMap;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.KnowledgeBaseKeyJsonDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.KnowledgeBaseKeyJsonSerializer;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnProcessingAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

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

  private Map<KnowledgeBase, ColumnProcessingTypeValue> processingType;

  public ColumnProcessingAnnotationValue() {
    processingType = ImmutableMap.of();
  }

  public ColumnProcessingAnnotationValue(ColumnProcessingAnnotation adaptee) {
    this.processingType = adaptee.getProcessingType();
  }

  /**
   * @return the processing type
   */
  @XmlAnyElement
  @JsonDeserialize(keyUsing = KnowledgeBaseKeyJsonDeserializer.class)
  @JsonSerialize(keyUsing = KnowledgeBaseKeyJsonSerializer.class)
  public Map<KnowledgeBase, ColumnProcessingTypeValue> getProcessingType() {
    return processingType;
  }

  /**
   * @param processingType the processing type to set
   */
  public void setProcessingType(
      Map<? extends KnowledgeBase, ? extends ColumnProcessingTypeValue> processingType) {
    final ImmutableMap.Builder<KnowledgeBase, ColumnProcessingTypeValue> processingTypeBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends ColumnProcessingTypeValue> processingTypeEntry : processingType
        .entrySet()) {
      processingTypeBuilder.put(processingTypeEntry.getKey(), processingTypeEntry.getValue());
    }

    this.processingType = processingTypeBuilder.build();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ColumnProcessingAnnotationValue [processingType=" + processingType + "]";
  }
}
