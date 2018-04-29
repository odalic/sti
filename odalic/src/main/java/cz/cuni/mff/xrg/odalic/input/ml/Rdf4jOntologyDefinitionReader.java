package cz.cuni.mff.xrg.odalic.input.ml;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.MLOntologyDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Rdf4jOntologyDefinitionReader implements OntologyDefinitionReader {


    @Override
    public MLOntologyDefinition readOntologyDefinitions(String[] fileNames) throws IOException {

        Model model = new LinkedHashModel();

        for (String fileName: fileNames) {

            try {
                Model fileModel = parseOntologyFile(fileName, "http://odalic.eu");

                // add statements to the "main" model
                model.addAll(fileModel);

                // copy namespaces as well
                fileModel.getNamespaces().stream()
                        .filter(ns -> !model.getNamespace(ns.getPrefix()).isPresent())
                        .forEach(model::setNamespace);

            } catch (Exception e) {
                throw new IOException("Failed to parse Ontology Definition file: '" + fileName + "'", e);
            }
        }

        MLOntologyDefinition mlOntologyDefinition = new MLOntologyDefinition(model);
        mlOntologyDefinition.buildPropertyModel();
        return mlOntologyDefinition;
    }

    private Model parseOntologyFile(String fileName, String baseUri) throws IOException, RDFParseException {
        File file = new File(fileName);
        FileInputStream fileInputStream = new FileInputStream(file);

        List<RDFFormat> supportedFormats = new ArrayList<>();
        supportedFormats.add(RDFFormat.TURTLE);
        supportedFormats.add(RDFFormat.RDFXML);
        supportedFormats.add(RDFFormat.NTRIPLES);

        Optional<RDFFormat> format = RDFFormat.matchFileName(fileName, supportedFormats);

        if (format.isPresent()) {
            return Rio.parse(fileInputStream, baseUri, RDFFormat.TURTLE);
        } else {
            throw new RDFParseException("Failed to parse file: " + fileName + ": unsupported format.");
        }
    }
}
