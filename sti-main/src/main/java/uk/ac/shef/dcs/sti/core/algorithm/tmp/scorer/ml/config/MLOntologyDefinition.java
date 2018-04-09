package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.config;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.exception.MLOntologyClassNotFoundException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.exception.MLOntologyPropertyNotFoundException;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class MLOntologyDefinition {

    private final Model ontologyModel;
    private final ValueFactory valueFactory;

    public MLOntologyDefinition(Model ontologyModel) {
        this.ontologyModel = ontologyModel;
        this.valueFactory = SimpleValueFactory.getInstance();
    }

    public Clazz loadClazz(String classUri) throws MLOntologyClassNotFoundException {
        Set<Resource> foundClassCandidates = ontologyModel.filter(this.valueFactory.createIRI(classUri), RDF.TYPE, OWL.CLASS).subjects();
        if (foundClassCandidates != null && !foundClassCandidates.isEmpty()) {
            Resource owlClass = foundClassCandidates.stream().findFirst().get();
            // try to load label for the class
            Set<Literal> labels = Models.objectLiterals(ontologyModel.filter(owlClass, RDFS.LABEL, null));
            if (!labels.isEmpty()) {
                Set<Literal> engLabels = labels
                        .stream()
                        .filter(lab -> lab.getLanguage().isPresent() && lab.getLanguage().get().equals("en"))
                        .collect(Collectors.toSet());

                if (!engLabels.isEmpty()) {
                    Literal engLabel = engLabels.stream().findFirst().get();
                    return new Clazz(classUri, engLabel.getLabel());
                } else {
                    Literal label = labels.stream().findFirst().get();
                    return new Clazz(classUri, label.getLabel());
                }
            } else {
                return new Clazz(classUri, null);
            }
        } else {
            // no such class found in ontology definitions, throw exception
            throw new MLOntologyClassNotFoundException(classUri);
        }
    }

    public Set<String> findDomainClassUrisOfPredicate(String predicateUri)
            throws MLOntologyPropertyNotFoundException {
        // try to find the domain of given Property. If there is no direct domain defined for given Property,
        // and the property is subProperty of other property, the domain lookup is recursively applied to the
        // 'parent' property.

        // Since there can be multiple subPropertyOf statements, their intersection is considered as domain of the property.
        // Therefore, once the BFS algorithm finds at least some Domain, we consider it to be a domain of the property.
        Queue<IRI> queue = new LinkedList<>();
        IRI rootProperty = this.valueFactory.createIRI(predicateUri);
        queue.add(rootProperty);

        IRI prop;
        while ((prop = queue.remove()) != null) {
            Set<IRI> domainIRIs = doFindDomainClassUrisOfPredicateBfs(prop, queue);

            if (!domainIRIs.isEmpty()) {
                return domainIRIs
                        .stream()
                        .map(IRI::stringValue)
                        .collect(Collectors.toSet());
            }
        }

        return new HashSet<>();
    }

    public String findPropertyForSubjectObject(String subjectUri, String objectUri) {
        // try to find object property, which has subjectUri in its direct domain and
        // objectUri in its direct range
        IRI subjectIRI = valueFactory.createIRI(subjectUri);
        IRI objectIRI = valueFactory.createIRI(objectUri);
        Set<Resource> props = ontologyModel.filter(null, RDF.TYPE, RDF.PROPERTY).subjects();
        for (Resource prop: props) {
            Set<Resource> psWithDomain = ontologyModel.filter(prop, RDFS.DOMAIN, subjectIRI).subjects();
            if (!psWithDomain.isEmpty()) {
                // if psWithDomain is not empty, there is a Domain match,
                // check if there is also Range match
                Set<Resource> psWithDomainRange = ontologyModel.filter(prop, RDFS.RANGE, objectIRI).subjects();
                if (!psWithDomainRange.isEmpty()) {
                    // there is match in both Domain and Range
                    return prop.stringValue();
                }
            }
        }
        return null;
    }

    private Set<IRI> doFindDomainClassUrisOfPredicateBfs(IRI currentProp, Queue<IRI> queue)
            throws MLOntologyPropertyNotFoundException {

        Set<Resource> properties = ontologyModel.filter(currentProp, RDF.TYPE, RDF.PROPERTY).subjects();
        if (!properties.isEmpty()) {
            Resource owlProperty = properties.stream().findFirst().get();

            Set<IRI> propDomainClassURIs = Models.objectIRIs(ontologyModel.filter(owlProperty, RDFS.DOMAIN, null));

            if (!propDomainClassURIs.isEmpty()) {
                return propDomainClassURIs;
            } else {
                // recursive call
                Set<IRI> parentPropURIs = Models.objectIRIs(ontologyModel.filter(owlProperty, RDFS.SUBPROPERTYOF, null));
                queue.addAll(parentPropURIs);
                return propDomainClassURIs;
            }
        }
        throw new MLOntologyPropertyNotFoundException(currentProp.stringValue());
    }
}
