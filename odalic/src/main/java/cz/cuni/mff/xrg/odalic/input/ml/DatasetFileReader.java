package cz.cuni.mff.xrg.odalic.input.ml;

import java.io.IOException;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.preprocessing.InputValue;

public interface DatasetFileReader {

    /**
     * Reads given filename and returns array of parsed InputValues.
     * @param fileName
     * @param configuration
     * @return
     */
    InputValue[] readDatasetFile(String fileName, Format configuration) throws IOException;
}
