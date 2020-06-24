package cz.cuni.mff.xrg.odalic.input.ml;

import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.input.ParsingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.preprocessing.InputValue;

import java.util.*;

public class InputValueFactory {

    private static final Logger LOG = LoggerFactory.getLogger(InputValueFactory.class);

    public static InputValue[] fromParsingResult(ParsingResult parsingResult) {
        Input datasetInput = parsingResult.getInput();

        // get column headers
        Map<Integer, String> columnHeaderMap = getHeaderMap(datasetInput);
        InputValue[] inputValues = parseInputValues(columnHeaderMap, datasetInput.rows());
        return inputValues;

    }

    private static Map<Integer, String> getHeaderMap(Input datasetInput) {
        Map<Integer, String> columnHeaderMap = new HashMap<>();
        for (int i = 0; i < datasetInput.headers().size(); i++) {
            columnHeaderMap.put(i, datasetInput.headers().get(i));
        }
        return columnHeaderMap;
    }

    private static InputValue[] parseInputValues(Map<Integer, String> headerMap, List<List<String>> rows) {

        Set<String> existingValueSet = new HashSet<>();
        List<InputValue> rowValues = new ArrayList<>();

        for (final List<String> row : rows) {
            rowValues.addAll(parseRowInputValues(row, headerMap, existingValueSet));
        }
        return rowValues.toArray(new InputValue[0]);
    }

    private static List<InputValue> parseRowInputValues(List<String> row, Map<Integer, String> headerMap, Set<String> existingValueSet) {

        List<InputValue> list = new ArrayList<>();
        int duplicates = 0;
        int emptyValues = 0;
        int column = 0;

        for (final String value : row) {
            if (!value.isEmpty()) {
                if (!existingValueSet.contains(value)) {
                    list.add(new InputValue(value, headerMap.get(column)));
                    existingValueSet.add(value);
                } else {
                    duplicates++;
                }
            } else {
                emptyValues++;
            }
            column++;
        }

        LOG.debug("Skipped " + duplicates + " duplicate and " + emptyValues + " empty values.");
        return list;
    }
}
