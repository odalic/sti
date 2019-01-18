/**
 * <p>
 * Classes of ExtraRelaTable post-processor, which allows to extend the results with those provided
 * by ERT algorithm. The post-processor itself is little more than a HTTP client which sends the
 * inputs as request to the public API of an ERT instance.
 * </p>
 * 
 * <p>
 * The main idea and justification of decisions leading to establishment of the post-processing
 * infrastructure within Odalic are described in thesis Discovering and Creating Relations among CSV
 * Columns using Linked Data Knowledge Bases.
 * </p>
 */
package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable;
