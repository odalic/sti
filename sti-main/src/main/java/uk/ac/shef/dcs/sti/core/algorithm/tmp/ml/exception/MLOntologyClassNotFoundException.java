package uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.exception;

public class MLOntologyClassNotFoundException extends MLOntologyEntityNotFoundException {

    public MLOntologyClassNotFoundException(String uri) {
        super(getErrMsg(uri));
    }

    public MLOntologyClassNotFoundException(String uri, Throwable cause) {
        super(getErrMsg(uri), cause);
    }

    private static String getErrMsg(String uri) {
        return "Class '" + uri + "' not found in Ontology Definitions!";
    }
}
