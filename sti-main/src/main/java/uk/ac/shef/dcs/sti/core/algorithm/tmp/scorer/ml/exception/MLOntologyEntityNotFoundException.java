package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.exception;

/**
 * Exception to be thrown when the OntologyDefinition Class/Predicate lookup fails.
 */
public class MLOntologyEntityNotFoundException extends Exception {

    public MLOntologyEntityNotFoundException(String message) {
        super(message);
    }

    public MLOntologyEntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
