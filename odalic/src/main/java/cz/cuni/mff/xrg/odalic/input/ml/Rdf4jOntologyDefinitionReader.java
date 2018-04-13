package cz.cuni.mff.xrg.odalic.input.ml;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.MLOntologyDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Rdf4jOntologyDefinitionReader implements OntologyDefinitionReader {


    @Override
    public MLOntologyDefinition readOntologyDefinitions(String[] fileNames) throws IOException {

        Model model = new LinkedHashModel();

        for (String fileName: fileNames) {

            try {
                File file = new File(fileName);
                FileInputStream fileInputStream = new FileInputStream(file);
                Model fileModel = Rio.parse(fileInputStream, "", RDFFormat.TURTLE);

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
}
