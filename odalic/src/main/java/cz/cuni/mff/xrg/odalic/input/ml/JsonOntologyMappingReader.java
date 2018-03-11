package cz.cuni.mff.xrg.odalic.input.ml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.preprocessing.MLOntologyMapping;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JsonOntologyMappingReader implements OntologyMappingReader {

    @SuppressWarnings("unchecked")
    public MLOntologyMapping readOntologyMapping(String ontologyMappingFilePath) throws IOException {
        JSONParser parser = new JSONParser();
        Map<String, String> map = new HashMap<>();

        try {
            Object obj = parser.parse(new FileReader(ontologyMappingFilePath));
            JSONObject jsonObject = (JSONObject) obj;
            Set<String> jsonKeySet = jsonObject.keySet();

            jsonKeySet.forEach(key -> map.put(key, (String) jsonObject.get(key)));
            return new MLOntologyMapping(map);
        } catch (ParseException e) {
            throw new IOException("Failed to parse JSON file: '" + ontologyMappingFilePath + "'", e);
        }
    }

}
