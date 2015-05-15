package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import cc.mallet.grmm.types.*;
import cc.mallet.types.LabelAlphabet;
import uk.ac.shef.dcs.oak.sti.rep.CellAnnotation;
import uk.ac.shef.dcs.oak.sti.rep.LTableAnnotation;

import java.util.*;

/**
 * Created by zqz on 12/05/2015.
 */
class FactorBuilderCell extends FactorBuilder{

    protected Map<Variable, int[]> cellVarOutcomePosition = new HashMap<Variable, int[]>();

    public Map<String, Variable> addFactors(LTableAnnotation annotation, FactorGraph graph,
                                            Map<Variable, String> typeOfVariable,
                                            Set<Integer> columns
    ) {
        Map<String, Variable> variables = new HashMap<String, Variable>();
        for (int row = 0; row < annotation.getRows(); row++) {
            for (int col = 0; col < annotation.getCols(); col++) {
                if(columns!=null&&!columns.contains(col)) continue;
                Variable dummyCell = createDummyVariable("dummyCell("+row+","+col+")");

                CellAnnotation[] candidateEntityAnnotations = annotation.getContentCellAnnotations(row, col);
                if (candidateEntityAnnotations.length == 0)
                    continue;
                String cellPosition = String.valueOf(row) + "," + String.valueOf(col);

                LabelAlphabet candidateIndex_cell = new LabelAlphabet();
                List<Double> scores=new ArrayList<Double>();
                for (int i = 0; i < candidateEntityAnnotations.length; i++) {
                    CellAnnotation ca = candidateEntityAnnotations[i];
                    double score=ca.getScore_element_map().get(
                            DisambiguationScorer_JI_adapted.SCORE_CELL_FACTOR);
                    if(score==0)
                        continue;
                    candidateIndex_cell.lookupIndex(ca.getAnnotation().getId());
                    scores.add(score);
                }
                if(scores.size()==0)
                    continue;
                double[] compatibility = new double[scores.size()];
                for(int i=0; i<compatibility.length;i++)
                    compatibility[i]=scores.get(i);
                Variable variable_cell = new Variable(candidateIndex_cell);
                variable_cell.setLabel(VariableType.CELL.toString() + "." + cellPosition);
                typeOfVariable.put(variable_cell, VariableType.CELL.toString());
                cellVarOutcomePosition.put(variable_cell, new int[]{row, col});

                if (isValidCompatibility(compatibility, null)) {
                    if(patchScores) compatibility= patchCompatibility(compatibility);
                    VarSet varSet = new HashVarSet(new Variable[]{dummyCell, variable_cell});
                    TableFactor factor = new TableFactor(varSet, compatibility);
                    graph.addFactor(factor);
                    variables.put(row + "," + col, variable_cell);
                }
            }
        }
        return variables;

    }
    public Map<String, Variable> addFactors(LTableAnnotation annotation, FactorGraph graph,
                                            Map<Variable, String> typeOfVariable
                                            ) {
        return addFactors(annotation, graph, typeOfVariable, null);
    }
}
