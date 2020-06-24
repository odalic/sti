package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.input.ml.TaskMLConfiguration;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.MLPreClassifier;

/**
 * This factory class loosely encapsulates the process of interpreter creation.
 *
 * @author Václav Brodec
 *
 */
public interface SemanticTableInterpreterFactory {

  /**
   * Lazily initializes the interpreter.
   *
   * @param userId the valid owner of the bases
   * @param bases the bases from which the interpreters are derived
   * @param mlPreClassifier mlPreclassifier which should be used
   * @return the interpreter implementations
   * @throws IOException when the initialization process fails to load its configuration
   * @throws STIException when the interpreters fail to initialize
   */
  Map<String, SemanticTableInterpreter> getInterpreters(final String userId, Set<? extends KnowledgeBase> bases,
                                                        final MLPreClassifier mlPreClassifier) throws STIException, IOException;

  /**
   * Initializes & Trains the ML PreClassification component.
   *
   * @param mlConfig ML configuration
   * @return trained ML PreClassifier
   * @throws STIException when the ML PreClassifier fails to initialize
   */
  MLPreClassifier getMLPreClassifier(final TaskMLConfiguration mlConfig) throws STIException;

}
