package cz.cuni.mff.xrg.odalic.tasks;

import com.google.common.base.Preconditions;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.entities.EntitiesService;
import cz.cuni.mff.xrg.odalic.entities.ResourceProposal;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.shef.dcs.kbproxy.ProxyException;

import java.net.URI;
import java.util.*;

public class DefaultAutoPropositionService implements AutoPropositionService {

    private static final Logger log = LoggerFactory.getLogger(DefaultAutoPropositionService.class);

    private final EntitiesService entitiesService;

    @Autowired
    public DefaultAutoPropositionService(final EntitiesService entitiesService) {
        Preconditions.checkNotNull(entitiesService, "The entitiesService cannot be null!");

        this.entitiesService = entitiesService;
    }

    @Override
    public void autoProposeNewResources(Task task, Input taskInput, Result taskResult, KnowledgeBase primaryKnowledgeBase) {

        final String knowledgeBaseName = primaryKnowledgeBase.getName();
        List<List<String>> inputRows = taskInput.rows();
        List<HeaderAnnotation> colHeaderAnnotations = taskResult.getHeaderAnnotations();

        // detect which columns are classified as an ontological class
        // column is classified as ontological class, if it has at least 1 chosen entity from primary knowledge base
        final Map<Integer, Entity> colClassesMap = new HashMap<>();
        for (int col = 0; col < colHeaderAnnotations.size(); col++) {
            final Entity chosenClassEntity =
                    getFirstChosenColumnClassEntityFromKnowledgeBase(colHeaderAnnotations.get(col), knowledgeBaseName);

            if (chosenClassEntity != null) {
                colClassesMap.put(col, chosenClassEntity);

            }
        }

        // for each row pick all cellValues from classified columns,
        // which are not empty and have no chosen entity candidate
        final Set<ProposedResourceKey> proposedResources = new HashSet<>();
        int rowIndex = 0;
        for (final List<String> row : inputRows) {
            int colIndex = -1;
            for (String col : row) {
                colIndex++;
                final String cellValue = col.trim();
                // skip unclassified columns and columns with empty cell value
                if (!colClassesMap.containsKey(colIndex) || cellValue.isEmpty() ) {
                    continue;
                }

                final Entity colClassEntity = colClassesMap.get(colIndex);
                if (!hasChosenEntityCandidate(taskResult.getCellAnnotations()[rowIndex][colIndex])) {
                    final ProposedResourceKey proposedResourceKey = new ProposedResourceKey(cellValue, colClassEntity);
                    if (!proposedResources.contains(proposedResourceKey)) {
                        // if there is no resource proposed for the (cell value, class) combination yet, propose a new resource

                        final NavigableSet<String> altLabels = new TreeSet<>();
                        final URI suffix = null; // should result in random UUID suffix
                        final Set<Entity> classes = new HashSet<>();
                        classes.add(colClassEntity);

                        final ResourceProposal resourceProposal = new ResourceProposal(cellValue, altLabels, suffix, classes);

                        try {
                            final Entity proposedResource = this.entitiesService.propose(primaryKnowledgeBase, resourceProposal);
                            // update set of proposed resources
                            proposedResources.add(proposedResourceKey);
                        } catch (ProxyException e) {
                            // log error
                            log.error("Failed to propose resource: " + resourceProposal.toString() +
                                    " to knowledge base: " + primaryKnowledgeBase.getName() +
                                    e.getMessage()
                            );
                        }
                    }
                }
            }
            rowIndex++;
        }
    }

    private Entity getFirstChosenColumnClassEntityFromKnowledgeBase(final HeaderAnnotation headerAnnotation,
                                                                    final String knowledgeBase) {
        final Set<EntityCandidate> candidatesKB = headerAnnotation.getChosen().get(knowledgeBase);
        if (!candidatesKB.isEmpty()) {
            return candidatesKB.iterator().next().getEntity();
        }
        return null;
    }

    private boolean hasChosenEntityCandidate(final CellAnnotation cellAnnotation) {
        final Map<String, Set<EntityCandidate>> chosenForCell = cellAnnotation.getChosen();
        for (Map.Entry<String, Set<EntityCandidate>> entry : chosenForCell.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    class ProposedResourceKey {
        private String cellValue;
        private Entity columnEntity;

        public ProposedResourceKey(String cellValue, Entity columnEntity) {
            this.cellValue = cellValue;
            this.columnEntity = columnEntity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ProposedResourceKey)) return false;
            ProposedResourceKey that = (ProposedResourceKey) o;
            return Objects.equals(cellValue, that.cellValue) &&
                    Objects.equals(columnEntity, that.columnEntity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cellValue, columnEntity);
        }
    }
}
