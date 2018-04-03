package cz.cuni.mff.xrg.odalic.input.ml;

import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.preprocessing.MLOntologyMapping;

import java.io.IOException;

public interface OntologyMappingReader {

    /**
     * Parses the ontology mappings from given file.
     * @param ontologyMappingFilePath
     * @return
     * @throws IOException
     */
    MLOntologyMapping readOntologyMapping(String ontologyMappingFilePath) throws IOException;

}
