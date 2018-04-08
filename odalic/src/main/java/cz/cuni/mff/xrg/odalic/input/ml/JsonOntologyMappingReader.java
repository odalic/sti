package cz.cuni.mff.xrg.odalic.input.ml;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.config.MLOntologyMapping;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonOntologyMappingReader implements OntologyMappingReader {

    private static String PROP_CLASS_MAPPINGS = "classMappings";
    private static String PROP_PREDICATE_MAPPINGS = "predicateMappings";
    private static String PROP_ML_CLASS = "mlClass";
    private static String PROP_URI = "uri";

    public MLOntologyMapping readOntologyMapping(String ontologyMappingFilePath) throws IOException {
        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader(ontologyMappingFilePath));
            JSONObject jsonObject = (JSONObject) obj;

            JSONArray jsonClassMappings = (JSONArray) jsonObject.get(PROP_CLASS_MAPPINGS);
            JSONArray jsonPredicateMappings = (JSONArray) jsonObject.get(PROP_PREDICATE_MAPPINGS);

            Map<String, String> classMappings = parseJsonMappings(jsonClassMappings);
            Map<String, String> predicateMappings = parseJsonMappings(jsonPredicateMappings);

            return new MLOntologyMapping(classMappings, predicateMappings);
        } catch (ParseException e) {
            throw new IOException("Failed to parse JSON file: '" + ontologyMappingFilePath + "'", e);
        }
    }

    private Map<String, String> parseJsonMappings(JSONArray mappingsArray) {
        Map<String, String> mappings = new HashMap<>();

        if (mappingsArray != null) {
            for (Object mapping : mappingsArray) {
                JSONObject jsonMapping = (JSONObject) mapping;
                String mlClass = (String) jsonMapping.get(PROP_ML_CLASS);
                String uri = (String) jsonMapping.get(PROP_URI);

                mappings.put(mlClass, uri);
            }
        }

        return mappings;
    }
}
