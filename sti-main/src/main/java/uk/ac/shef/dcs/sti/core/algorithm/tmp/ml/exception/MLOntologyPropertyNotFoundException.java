package uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.exception;

public class MLOntologyPropertyNotFoundException extends MLOntologyEntityNotFoundException {

    public MLOntologyPropertyNotFoundException(String uri) {
        super(getErrMsg(uri));
    }

    public MLOntologyPropertyNotFoundException(String uri, Throwable cause) {
        super(getErrMsg(uri), cause);
    }

    private static String getErrMsg(String uri) {
        return "Property '" + uri + "' not found in Ontology Definitions!";
    }

}
