package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import javafx.util.Pair;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.KBProxyResult;
import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.core.model.DisambiguationResult;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.scorer.EntityScorer;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.*;

/**
 */
public class TCellDisambiguator {

    private KBProxy kbSearch;
    private EntityScorer disambScorer;
    private static final Logger LOG = LoggerFactory.getLogger(TCellDisambiguator.class.getName());

    public TCellDisambiguator(KBProxy kbSearch, EntityScorer disambScorer) {
        this.kbSearch = kbSearch;
        this.disambScorer = disambScorer;
    }
    //this method runs cold start disambiguation
    public DisambiguationResult coldstartDisambiguate(
            List<Entity> candidates,
            Table table,
            List<Integer> entity_rows,
            int entity_column
    ) {
        LOG.info("\t\t>> (cold start disamb), candidates=" + candidates.size());
        return disambiguate(candidates, table,entity_rows,entity_column);
    }

    //reselect cell entities for this cell ensuring their types are contained in the winning clazz for the column
    public TCellAnnotation[] reselect(
            TCellAnnotation[] existingCellAnnotations,
            Collection<String> winningClazzIdsForColumn) {
        List<TCellAnnotation> selected = new ArrayList<>();
        for(TCellAnnotation tca: existingCellAnnotations){
            int overlap = CollectionUtils.intersection(tca.getAnnotation().getTypeIds(),
                    winningClazzIdsForColumn).size();
            if(overlap>0)
                selected.add(tca);
        }

        return selected.toArray(new TCellAnnotation[0]);
    }

    public DisambiguationResult constrainedDisambiguate(
            List<Entity> candidates,
            Table table,
            List<Integer> rowBlock,
            int column,
            int totalRowBlocks,
            boolean isLEARNINGPhase) {
        TCell sample_tcc = table.getContentCell(rowBlock.get(0), column);
        if (isLEARNINGPhase)
            LOG.info("\t\t>> (constrained disambiguation in LEARNING) , position at (" + rowBlock + "/"+totalRowBlocks+"," + column + ") " + sample_tcc + " candidates=" + candidates.size());
        else
            LOG.info("\t\t>> (constrained disambiguation in UPDATE), position at (" + rowBlock + "/"+totalRowBlocks+"," + column + ") " + sample_tcc + " (candidates)-" + candidates.size());

        return disambiguate(candidates, table, rowBlock,column);
    }

    public DisambiguationResult disambiguate(
            List<Entity> candidates,
            Table table,
            List<Integer> entity_rows,
            int entity_column) {
        //do disambiguation scoring
        //LOG.info("\t>> Disambiguation-LEARN, position at (" + entity_row + "," + entity_column + ") candidates=" + candidates.size());
        TCell sample_tcc = table.getContentCell(entity_rows.get(0), entity_column);
        List<Pair<Entity, Map<String, Double>>> disambiguationScores = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (Entity c : candidates) {
            //find facts of each entity
            if (c.getAttributes() == null || c.getAttributes().size() == 0) {
                KBProxyResult<List<Attribute>> attributesResult = kbSearch.findAttributesOfEntities(c);

                c.setAttributes(attributesResult.getResult());
                attributesResult.appendWarning(warnings);
            }
            Map<String, Double> scoreMap = disambScorer.
                    computeElementScores(c, candidates,
                            entity_column,
                            entity_rows.get(0),
                            entity_rows, table);
            disambScorer.computeFinal(scoreMap, sample_tcc.getText());
            Pair<Entity, Map<String, Double>> entry = new Pair<>(c, scoreMap);
            disambiguationScores.add(entry);
        }
        return new DisambiguationResult(disambiguationScores, warnings);
    }

    protected void addCellAnnotation(
            Table table,
            TAnnotation tableAnnotation,
            List<Integer> rowBlock,
            int table_cell_col,
            DisambiguationResult entities_and_scoreMap) {

        Collections.sort(entities_and_scoreMap.getResult(), (o1, o2) -> {
            Double o2_score = o2.getValue().get(TCellAnnotation.SCORE_FINAL);
            Double o1_score = o1.getValue().get(TCellAnnotation.SCORE_FINAL);
            return o2_score.compareTo(o1_score);
        });

        String cellText = table.getContentCell(rowBlock.get(0), table_cell_col).getText();
        for (int row : rowBlock) {
            TCellAnnotation[] annotationsForCell = new TCellAnnotation[entities_and_scoreMap.getResult().size()];
            for (int i = 0; i < entities_and_scoreMap.getResult().size(); i++) {
                Pair<Entity, Map<String, Double>> e = entities_and_scoreMap.getResult().get(i);
                double score = e.getValue().get(TCellAnnotation.SCORE_FINAL);
                annotationsForCell[i] = new TCellAnnotation(cellText,
                        e.getKey(), score, e.getValue());

            }

            tableAnnotation.setContentCellAnnotations(row, table_cell_col, annotationsForCell);
            tableAnnotation.addContentWarnings(row, table_cell_col, entities_and_scoreMap.getWarnings());
        }
    }

}
