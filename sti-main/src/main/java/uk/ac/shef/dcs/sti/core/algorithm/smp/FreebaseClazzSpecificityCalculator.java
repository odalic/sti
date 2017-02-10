package uk.ac.shef.dcs.sti.core.algorithm.smp;

import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.kbproxy.KBProxyResult;
import uk.ac.shef.dcs.sti.STIConstantProperty;

/**
 *
 */
public class FreebaseClazzSpecificityCalculator implements ClazzSpecificityCalculator {

    private KBProxy kbSearch;

    public FreebaseClazzSpecificityCalculator(KBProxy kbProxy) {
        this.kbSearch = kbProxy;
    }

    @Override
    public double compute(String clazzURI) {
        KBProxyResult<Double> conceptGranularityResult = kbSearch.findGranularityOfClazz(clazzURI);
        double conceptGranularity = conceptGranularityResult.getResult();

        if (conceptGranularity < 0)
            return 0.0;
        return 1 - Math.sqrt(conceptGranularity / STIConstantProperty.FREEBASE_TOTAL_TOPICS);
    }
}
