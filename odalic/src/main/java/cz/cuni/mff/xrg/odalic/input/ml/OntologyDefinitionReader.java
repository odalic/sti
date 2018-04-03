package cz.cuni.mff.xrg.odalic.input.ml;

import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.preprocessing.MLOntologyDefinition;

import java.io.IOException;

public interface OntologyDefinitionReader {

    /**
     * Loads Ontology definitions from given files.
     * @param fileNames
     * @return
     */
    MLOntologyDefinition readOntologyDefinitions(String[] fileNames) throws IOException;

}
