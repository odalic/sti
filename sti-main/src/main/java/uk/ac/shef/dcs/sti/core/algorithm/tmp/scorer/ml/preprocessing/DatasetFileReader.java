package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.preprocessing;

import java.io.IOException;

public interface DatasetFileReader {

    /**
     * Reads given filename and returns array of parsed InputValues.
     * @param filename
     * @return
     */
    InputValue[] readFile(String filename) throws IOException;
}
