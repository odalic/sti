package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;

/**
 * Extension for column processing annotation
 */
public class TColumnProcessingAnnotation implements Serializable {

    private static final long serialVersionUID = -1208912663212074692L;

    private TColumnProcessingType processingType;

    public TColumnProcessingAnnotation(TColumnProcessingType processingType) {
        this.processingType = processingType;
    }

    public TColumnProcessingType getProcessingType() {
        return processingType;
    }

    public boolean equals(Object o) {
        if(o instanceof TColumnProcessingAnnotation) {
            TColumnProcessingAnnotation hbr = (TColumnProcessingAnnotation) o;
            return hbr.getProcessingType().equals(getProcessingType());
        }
        return false;
    }

    public enum TColumnProcessingType implements Serializable {
        NAMED_ENTITY,
        NON_NAMED_ENTITY,
        IGNORED
    }
}
