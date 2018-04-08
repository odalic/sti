package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.exception;

/**
 * Exception to be thrown on Machine Learning Classifier failure.
 */
public class MLException extends Exception {

    public MLException(String message) {
        super(message);
    }

    public MLException(String message, Throwable cause) {
        super(message, cause);
    }
}
