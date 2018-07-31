package uk.ac.shef.dcs.kbproxy.sparql.pp.helpers;

/**
 * Created by tomasknap on 09/02/17.
 */
public class PPRestApiCallException extends RuntimeException {

    private static final long serialVersionUID = -1315176954713728038L;

	public PPRestApiCallException(String errorMsg) {
        super(errorMsg);
    }

    public PPRestApiCallException(String errorMsg, Exception ex) {
        super(errorMsg,ex);
    }
}
