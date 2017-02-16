package cz.cuni.mff.xrg.odalic.tasks.annotations;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.ColumnProcessingAnnotationAdapter;
import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnProcessingTypeValue;

/**
 * Annotates column in a table with processing type.
 * 
 * @author Josef Janoušek
 *
 */
@Immutable
@XmlJavaTypeAdapter(ColumnProcessingAnnotationAdapter.class)
public final class ColumnProcessingAnnotation implements Serializable {

  private static final long serialVersionUID = -1695807148169080424L;

  private final Map<KnowledgeBase, ColumnProcessingTypeValue> processingType;

  /**
   * Creates new annotation.
   * 
   * @param processingType type of statistical component
   */
  public ColumnProcessingAnnotation(
      Map<? extends KnowledgeBase, ? extends ColumnProcessingTypeValue> processingType) {
    Preconditions.checkNotNull(processingType);

    final ImmutableMap.Builder<KnowledgeBase, ColumnProcessingTypeValue> processingTypeBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends ColumnProcessingTypeValue> componentEntry : processingType
        .entrySet()) {
      processingTypeBuilder.put(componentEntry.getKey(), componentEntry.getValue());
    }
    this.processingType = processingTypeBuilder.build();
  }

  /**
   * @return the processing type
   */
  public Map<KnowledgeBase, ColumnProcessingTypeValue> getProcessingType() {
    return processingType;
  }

  /**
   * Merges with the other annotation.
   * 
   * @param other annotation based on different set of knowledge bases
   * @return merged annotation
   * @throws IllegalArgumentException If both this and the other annotation have some candidates
   *         from the same knowledge base
   */
  public ColumnProcessingAnnotation merge(ColumnProcessingAnnotation other) throws IllegalArgumentException {
    final ImmutableMap.Builder<KnowledgeBase, ColumnProcessingTypeValue> processingTypeBuilder =
        ImmutableMap.builder();
    processingTypeBuilder.putAll(this.processingType);
    processingTypeBuilder.putAll(other.processingType);

    return new ColumnProcessingAnnotation(processingTypeBuilder.build());
  }

  /**
   * Computes hash code based on the component and the predicate.
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((processingType == null) ? 0 : processingType.hashCode());
    return result;
  }

  /**
   * Compares for equality (only other annotation of the same processing type passes).
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ColumnProcessingAnnotation other = (ColumnProcessingAnnotation) obj;
    if (processingType == null) {
      if (other.processingType != null) {
        return false;
      }
    } else if (!processingType.equals(other.processingType)) {
      return false;
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ColumnProcessingAnnotation [processingType=" + processingType + "]";
  }
}
