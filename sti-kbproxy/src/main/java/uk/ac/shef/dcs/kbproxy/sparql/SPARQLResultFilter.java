package uk.ac.shef.dcs.kbproxy.sparql;

import uk.ac.shef.dcs.kbproxy.KBProxyResultFilter;

import java.io.IOException;

/**
 *
 */
public class SPARQLResultFilter extends KBProxyResultFilter {
    public SPARQLResultFilter(String property) throws IOException {
        super(property);
    }
}
