package uk.ac.shef.dcs.oak.sti.table.interpreter.baseline;

import uk.ac.shef.dcs.oak.sti.PlaceHolder;
import uk.ac.shef.dcs.oak.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.oak.sti.nlp.NLPTools;
import uk.ac.shef.dcs.oak.sti.table.rep.*;
import uk.ac.shef.dcs.oak.sti.test.TableMinerConstants;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.util.ObjObj;
import uk.ac.shef.dcs.oak.util.StringUtils;
import uk.ac.shef.wit.simmetrics.similaritymetrics.*;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 11/03/14
 * Time: 12:19
 * To change this template use File | Settings | File Templates.
 */
public class Base_TM_no_Update_ClassificationScorer {
    private Lemmatizer lemmatizer;
    private List<String> stopWords;
    //private Levenshtein stringSimilarityMetric;
    //private Jaro stringSimilarityMetric;
    //private Levenshtein stringSimilarityMetric;
    private AbstractStringMetric stringSimilarityMetric;

    public Base_TM_no_Update_ClassificationScorer(String nlpResources, List<String> stopWords,
                                                  double[] weights) throws IOException {
        this.lemmatizer = NLPTools.getInstance(nlpResources).getLemmatizer();
        this.stopWords = stopWords;
        this.stringSimilarityMetric = new Levenshtein();
        //this.stringSimilarityMetric=new CosineSimilarity();
    }

    public Set<HeaderAnnotation> score(List<ObjObj<EntityCandidate, Map<String, Double>>> input,
                                       Set<HeaderAnnotation> headerAnnotations_prev,
                                       LTable table,
                                       int row, int column) {
        Set<HeaderAnnotation> candidates=new HashSet<HeaderAnnotation>();
        if (TableMinerConstants.CLASSIFICATION_CANDIDATE_CONTRIBUTION_METHOD == 1)
            candidates = score_entity_all_candidate_vote(input, headerAnnotations_prev, table, row, column);
        else
            candidates = score_entity_best_candidate_vote(input, headerAnnotations_prev, table, row, column);
        candidates = score_context(candidates, table, column, false);

        return candidates;
    }

    public Set<HeaderAnnotation> score_entity_best_candidate_vote(List<ObjObj<EntityCandidate, Map<String, Double>>> input,
                                                                  Set<HeaderAnnotation> headerAnnotations_prev, LTable table,
                                                                  int row, int column) {
        final Set<HeaderAnnotation> candidate_header_annotations =
                headerAnnotations_prev;
        //for this row
        EntityCandidate entity_with_highest_disamb_score = null;
        double best_score = 0.0;
        for (ObjObj<EntityCandidate, Map<String, Double>> es : input) { //each candidate entity in this cell
            EntityCandidate entity = es.getMainObject();
            //each assigned type receives a score of 1, and the bonus score due to disambiguation result
            double entity_disamb_score = es.getOtherObject().get(CellAnnotation.SCORE_FINAL);
            if (entity_disamb_score > best_score) {
                best_score = entity_disamb_score;
                entity_with_highest_disamb_score = entity;
            }
        }
        if (input.size() == 0 || entity_with_highest_disamb_score == null) {
            //this entity has a score of 0.0, it should not contribute to the header typing, but we may still keep it as candidate for this cell
            System.out.print("x(" + row + "," + column + ")");
            return candidate_header_annotations;
        }


        if (input.size() == 0) {
            //this entity has a score of 0.0, it should not contribute to the header typing, but we may still keep it as candidate for this cell
            System.out.print("x(" + row + "," + column + ")");
            return candidate_header_annotations;
        }

        Set<String> types_already_received_votes_by_cell = new HashSet<String>();    //each type will receive a max of 1 vote from each cell. If multiple candidates have the same highest score and casts same votes, they are counted oly once
        for (ObjObj<EntityCandidate, Map<String, Double>> es : input) {
            EntityCandidate current_candidate = es.getMainObject();
            double entity_disamb_score = es.getOtherObject().get(CellAnnotation.SCORE_FINAL);
            if (entity_disamb_score != best_score)
                continue;

            List<String[]> type_voted_by_this_cell = current_candidate.getTypes();

            //consolidate scores from this cell
            for (String[] type : type_voted_by_this_cell) {
                if (TableMinerConstants.BEST_CANDIDATE_CONTRIBUTE_COUNT_ONLY_ONCE
                        && types_already_received_votes_by_cell.contains(type[0]))
                    continue;

                types_already_received_votes_by_cell.add(type[0]);
                String headerText = table.getColumnHeader(column).getHeaderText();

                HeaderAnnotation hAnnotation = null;
                for (HeaderAnnotation key : candidate_header_annotations) {
                    if (key.getTerm().equals(headerText) && key.getAnnotation_url().equals(type[0]
                    )) {
                        hAnnotation = key;
                        break;
                    }
                }
                if (hAnnotation == null) {
                    hAnnotation = new HeaderAnnotation(headerText, type[0], type[1], 0.0);
                }

                Map<String, Double> tmp_score_elements = hAnnotation.getScoreElements();
                if (tmp_score_elements == null || tmp_score_elements.size() == 0) {
                    tmp_score_elements = new HashMap<String, Double>();
                    tmp_score_elements.put(HeaderAnnotation.SUM_ENTITY_VOTE, 0.0);
                }
                tmp_score_elements.put(HeaderAnnotation.SUM_ENTITY_VOTE,
                        tmp_score_elements.get(HeaderAnnotation.SUM_ENTITY_VOTE) + 1.0);
                hAnnotation.setScoreElements(tmp_score_elements);

                candidate_header_annotations.add(hAnnotation);
            }
        }

        return candidate_header_annotations;
    }


    public Set<HeaderAnnotation> score_entity_all_candidate_vote(List<ObjObj<EntityCandidate, Map<String, Double>>> input,
                                                                 Set<HeaderAnnotation> headerAnnotations_prev, LTable table,
                                                                 int row, int column) {
        final Set<HeaderAnnotation> candidate_header_annotations =
                headerAnnotations_prev;

        if (input.size() == 0) {
            //this entity has a score of 0.0, it should not contribute to the header typing, but we may still keep it as candidate for this cell
            System.out.print("x(" + row + "," + column + ")");
            return candidate_header_annotations;
        }

        for (ObjObj<EntityCandidate, Map<String, Double>> es : input) {
            EntityCandidate current_candidate = es.getMainObject();

            List<String[]> type_voted_by_this_cell = current_candidate.getTypes();

            //consolidate scores from this cell
            for (String[] type : type_voted_by_this_cell) {
                String headerText = table.getColumnHeader(column).getHeaderText();

                HeaderAnnotation hAnnotation = null;
                for (HeaderAnnotation key : candidate_header_annotations) {
                    if (key.getTerm().equals(headerText) && key.getAnnotation_url().equals(type[0]
                    )) {
                        hAnnotation = key;
                        break;
                    }
                }
                if (hAnnotation == null) {
                    hAnnotation = new HeaderAnnotation(headerText, type[0], type[1], 0.0);
                }

                Map<String, Double> tmp_score_elements = hAnnotation.getScoreElements();
                if (tmp_score_elements == null || tmp_score_elements.size() == 0) {
                    tmp_score_elements = new HashMap<String, Double>();
                    tmp_score_elements.put(HeaderAnnotation.SUM_ENTITY_VOTE, 0.0);
                }
                tmp_score_elements.put(HeaderAnnotation.SUM_ENTITY_VOTE,
                        tmp_score_elements.get(HeaderAnnotation.SUM_ENTITY_VOTE) + 1.0);
                hAnnotation.setScoreElements(tmp_score_elements);

                candidate_header_annotations.add(hAnnotation);
            }
        }

        return candidate_header_annotations;
    }

    public Set<HeaderAnnotation> score_context(Set<HeaderAnnotation> candidates, LTable table, int column, boolean overwrite) {
        for (HeaderAnnotation ha : candidates) {
            Double score_ctx_header_text = ha.getScoreElements().get(HeaderAnnotation.SCORE_CTX_NAME_MATCH);

            if (score_ctx_header_text == null) {
                LTableColumnHeader header = table.getColumnHeader(column);
                if (header != null &&
                        header.getHeaderText() != null &&
                        !header.getHeaderText().equals(PlaceHolder.TABLE_HEADER_UNKNOWN.getValue())) {
                    //double score = CollectionUtils.diceCoefficientOptimized(header.getHeaderText(), ha.getAnnotation_label());
                    double score = stringSimilarityMetric.getSimilarity(
                            StringUtils.toAlphaNumericWhitechar(header.getHeaderText()),
                            StringUtils.toAlphaNumericWhitechar(ha.getAnnotation_label()));
                    ha.getScoreElements().put(HeaderAnnotation.SCORE_CTX_NAME_MATCH, score);
                }
            }
        }


        return candidates;
    }


    public Map<String, Double> compute_final_score(HeaderAnnotation ha, int tableRowsTotal) {
        Map<String, Double> scoreElements = ha.getScoreElements();
        double sum_entity_vote = scoreElements.get(HeaderAnnotation.SUM_ENTITY_VOTE);
        double score_entity_vote = sum_entity_vote / (double) tableRowsTotal;
        scoreElements.put(HeaderAnnotation.SCORE_ENTITY_VOTE, score_entity_vote);

        double finalScore = score_entity_vote;
        Double namematch = scoreElements.get(HeaderAnnotation.SCORE_CTX_NAME_MATCH);
        if (namematch != null) {
            finalScore = finalScore + namematch;
        }

        scoreElements.put(HeaderAnnotation.FINAL, finalScore);
        ha.setFinalScore(finalScore);
        return scoreElements;
    }


}
