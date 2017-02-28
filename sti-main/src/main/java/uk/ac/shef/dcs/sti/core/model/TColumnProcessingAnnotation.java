package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;

/**
 * Extension for column processing annotation
 */
public class TColumnProcessingAnnotation implements Serializable {

  public enum TColumnProcessingType implements Serializable {
    NAMED_ENTITY, NON_NAMED_ENTITY, IGNORED
  }

  private static final long serialVersionUID = -1208912663212074692L;

  private final TColumnProcessingType processingType;

  public TColumnProcessingAnnotation(final TColumnProcessingType processingType) {
    this.processingType = processingType;
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof TColumnProcessingAnnotation) {
      final TColumnProcessingAnnotation hbr = (TColumnProcessingAnnotation) o;
      return hbr.getProcessingType().equals(getProcessingType());
    }
    return false;
  }

  public TColumnProcessingType getProcessingType() {
    return this.processingType;
  }
}
