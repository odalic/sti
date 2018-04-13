package cz.cuni.mff.xrg.odalic.input.ml;

import com.google.common.base.Preconditions;
import cz.cuni.mff.xrg.odalic.files.formats.ApacheCsvFormatAdapter;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.preprocessing.InputValue;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Dataset file reader, which is able to parse InputValues from
 * CSV file-s, with '|' cell separator. This file reader ignores duplicit
 * values in input file.
 * The First Row of the input file should contain the class names of classes, which
 * are represented by the entities in following columns.
 */
public class CsvDatasetFileReader implements DatasetFileReader {

    private final ApacheCsvFormatAdapter apacheCsvFormatAdapter;

    private static final Logger LOG = LoggerFactory.getLogger(CsvDatasetFileReader.class);

    @Autowired
    public CsvDatasetFileReader(final ApacheCsvFormatAdapter apacheCsvFormatAdapter) {
        Preconditions.checkNotNull(apacheCsvFormatAdapter, "The apacheCsvFormatAdapter cannot be null!");

        this.apacheCsvFormatAdapter = apacheCsvFormatAdapter;
    }

    @Override
    public InputValue[] readDatasetFile(final URL fileName, final Format configuration) throws IOException {

        InputStream inputStream = fileName.openStream();
        Reader reader = new InputStreamReader(inputStream, configuration.getCharset());

        final CSVFormat format = this.apacheCsvFormatAdapter.toApacheCsvFormat(configuration);
        final CSVParser parser = format.parse(reader);

        // get csv column headers
        Map<Integer, String> columnHeaderMap = parseDatasetFileHeader(parser);

        // get csv row contents
        Set<String> existingValueSet = new HashSet<>();
        List<InputValue> csvRowValues = new ArrayList<>();
        int row = 0;
        for (final CSVRecord record : parser) {
            csvRowValues.addAll(parseDatasetLine(record, row, columnHeaderMap, existingValueSet));
            row++;
        }
        return csvRowValues.toArray(new InputValue[0]);
    }

    private Map<Integer, String> parseDatasetFileHeader(CSVParser parser) {
        // Map<StringValue, position>
        Map<String, Integer> valPosHeaderMap = parser.getHeaderMap();
        // change to Map<position, StringValue>
        Map<Integer, String> posValHeaderMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry: valPosHeaderMap.entrySet()) {
            posValHeaderMap.put(entry.getValue(), entry.getKey());
        }
        return posValHeaderMap;
    }

    private List<InputValue> parseDatasetLine(final CSVRecord row, final int rowIndex,
                                              Map<Integer, String> columnHeaderMap, Set<String> existingValueSet) throws IOException {

        if (!row.isConsistent()) {
            throw new IOException("CSV file is not consistent: data row with index " + rowIndex
                    + " has different size than header row.");
        }

        List<InputValue> list = new ArrayList<>();
        int duplicates = 0;
        int emptyValues = 0;
        int column = 0;

        for (final String value : row) {
            if (!value.isEmpty()) {
                if (!existingValueSet.contains(value)) {
                    list.add(new InputValue(value, columnHeaderMap.get(column)));
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
