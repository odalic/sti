package uk.ac.shef.dcs.sti.core.algorithm.ji;

import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.KBProxyResult;
import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.core.model.DisambiguationResult;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.util.StringUtils;

import java.util.*;

/**
 * Created by zqz on 01/05/2015.
 */
public class CandidateEntityGenerator {
    private KBProxy kbSearch;
    private JIAdaptedEntityScorer disambScorer;
    private static final Logger LOG = LoggerFactory.getLogger(CandidateEntityGenerator.class.getName());

    public CandidateEntityGenerator(KBProxy kbSearch, JIAdaptedEntityScorer disambScorer) {
        this.kbSearch = kbSearch;
        this.disambScorer = disambScorer;
    }

    public void generateInitialCellAnnotations(
            TAnnotation tableAnnotations,
            Table table,
            int row,
            int column) {
        TCell tcc = table.getContentCell(row, column);
        String text = StringUtils.toAlphaNumericWhitechar(tcc.getText().trim()).trim();
        if (text.length() > 2) {
            DisambiguationResult scores = scoreEntities(table, row, column);
            TCellAnnotation[] annotations = new TCellAnnotation[scores.getResult().size()];
            int i = 0;
            for (Pair<Entity, Map<String, Double>> oo : scores.getResult()) {
                TCellAnnotation ca = new TCellAnnotation(tcc.getText(),
                        oo.getKey(), oo.getValue().get(JIAdaptedEntityScorer.SCORE_FINAL),
                        oo.getValue());
                annotations[i] = ca;
                i++;
            }

            tableAnnotations.setContentCellAnnotations(row, column, annotations);
            tableAnnotations.addContentWarnings(row, column, scores.getWarnings());
        }
    }

    private DisambiguationResult scoreEntities(
            Table table,
            int row,
            int column) {
        TCell cell = table.getContentCell(row, column);
        LOG.info("\t\t>> (generating candidate entities, position at (" + row + "," + column + ") " +
                cell+")");

        List<String> cellWarnings = new ArrayList<>();
        KBProxyResult<List<Entity>> candidatesResult = kbSearch.findEntityCandidates(cell.getText());
        candidatesResult.appendWarning(cellWarnings);

        List<Entity> candidates = new ArrayList<>();
        for (Entity ec : candidatesResult.getResult()) {
            if (!candidates.contains(ec))
                candidates.add(ec);
        }

        //each candidate will have a map containing multiple elements of scores. See SMPAdaptedEntityScorer
        List<Pair<Entity, Map<String, Double>>> disambiguationScores = new ArrayList<>();
        for (Entity entity : candidates) {
            //find facts of each entity
            if (entity.getAttributes() == null || entity.getAttributes().size() == 0) {
                KBProxyResult<List<Attribute>> factsResult = kbSearch.findAttributesOfEntities(entity);
                factsResult.appendWarning(cellWarnings);

                entity.setAttributes(factsResult.getResult());
            }
            Map<String, Double> scoreMap = disambScorer.
                    computeElementScores(entity, candidates,
                            column, row, Collections.singletonList(row),
                            table);
            disambScorer.computeFinal(scoreMap, cell.getText());
            Pair<Entity, Map<String, Double>> entry = new Pair<>(entity, scoreMap);
            disambiguationScores.add(entry);
        }
        return new DisambiguationResult(disambiguationScores, cellWarnings);
    }
}
