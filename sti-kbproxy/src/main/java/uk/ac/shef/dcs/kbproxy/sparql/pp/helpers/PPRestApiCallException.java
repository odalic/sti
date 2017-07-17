package uk.ac.shef.dcs.kbproxy.sparql.pp.helpers;

/**
 * Created by tomasknap on 09/02/17.
 */
public class PPRestApiCallException extends RuntimeException {

    public PPRestApiCallException(String errorMsg) {
        super(errorMsg);
    }

    public PPRestApiCallException(String errorMsg, Exception ex) {
        super(errorMsg,ex);
    }
}
