package uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.EmptyModel;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.exception.MLOntologyClassNotFoundException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.exception.MLOntologyPropertyNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

public class MLOntologyDefinition {

    private final Model ontologyModel;
    private final ValueFactory valueFactory;

    private final Map<String, PropertyWithDomain> propertyModel = new HashMap<>();

    public static MLOntologyDefinition empty() {
        return new MLOntologyDefinition(new EmptyModel(new LinkedHashModel()));
    }

    public MLOntologyDefinition(Model ontologyModel) {
        this.ontologyModel = ontologyModel;
        this.valueFactory = SimpleValueFactory.getInstance();
    }

    /**
     * Build a model of all properties, where each property knows its directly defined Domain and list of all
     * super-properties.
     * The property should be then available to infer its domain also from its super-properties, if its not directly defined.
     */
    public void buildPropertyModel() {
        Set<Resource> allProperties = getAllProperties();
        for (Resource property : allProperties) {
            String propertyIRI = property.stringValue();
            Model propertyStatements = ontologyModel.filter(property, null, null);
            // check if the property has a domain defined
            Set<String> directDomainIRIs = detectDirectDomainURIsForProperty(propertyStatements, property);

            // check if the property is a subProperty of some other property
            Set<String> superPropertyIRIs = detectSuperPropertiesOfProperty(propertyStatements, property);

            // check if there are entities representing superPropertyIRIs in the model, if not, add 'placeholders'
            //for them
            Set<PropertyWithDomain> superProperties = retrieveSuperPropertyEntitiesOrPlaceholders(propertyIRI, superPropertyIRIs);

            // there should be just 1 definition of a property in ontology (ontologies), so
            // it should be safe to do this
            PropertyWithDomain existingProp = propertyModel.get(propertyIRI);
            if (existingProp != null) {
                // update existing (placeholder) property of model
                propertyModel.put(propertyIRI,
                        existingProp.withSuperPropertiesAndDirectDomain(superProperties, directDomainIRIs));
            } else {
                // add new property to model
                propertyModel.put(propertyIRI, new PropertyWithDomain(propertyIRI, superProperties, directDomainIRIs));
            }
        }
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

    /**
     * Find the class IRIs, which belong to the domain of given predicate.
     * If the domain is not restricted, resulting set will be empty.
     * If the domain is conflicting (e.g. the property has 2 superclasses, intersection of whose domains is empty), and
     * this there is nothing what can fullfill the domain criteria, null is returned.
     *
     * @param propertyIRI
     * @return
     * @throws MLOntologyPropertyNotFoundException
     */
    public Set<String> findDomainClassIRIsOfProperty(String propertyIRI)
            throws MLOntologyPropertyNotFoundException {

        PropertyWithDomain property = this.propertyModel.get(propertyIRI);
        if (property != null) {
            return property.getDomainIRIs();
        } else {
            throw new MLOntologyPropertyNotFoundException(propertyIRI);
        }
    }

    private Set<String> detectDirectDomainURIsForProperty(Model propertyStatements, Resource property) {
        Set<IRI> domainResources = Models.objectIRIs(propertyStatements.filter(property, RDFS.DOMAIN, null));
        return domainResources.stream().map(IRI::stringValue).collect(Collectors.toSet());
    }

    private Set<String> detectSuperPropertiesOfProperty(Model propertyStatements, Resource property) {
        Set<IRI> domainResources = Models.objectIRIs(propertyStatements.filter(property, RDFS.SUBPROPERTYOF, null));
        return domainResources.stream().map(IRI::stringValue).collect(Collectors.toSet());
    }

    private Set<PropertyWithDomain> retrieveSuperPropertyEntitiesOrPlaceholders(String propertyIRI, Set<String> superPropertyIRIs) {
        return superPropertyIRIs
            .stream()
            .map(spIRI -> {
                PropertyWithDomain existing = propertyModel.get(spIRI);
                if (existing != null) {
                    return existing;
                } else {
                    // add placeholder for the property and return it
                    PropertyWithDomain placeholder = new PropertyWithDomain(propertyIRI);
                    propertyModel.put(spIRI, placeholder);
                    return placeholder;
                }
            })
            .collect(Collectors.toSet());
    }


    public String findPropertyForSubjectObject(String subjectIRI, String objectIRI) {
        // At first try to find property, which has subject in its direct domain and
        // object in its direct range
        String directProperty = findDirectPropertyForSubjectObject(subjectIRI, objectIRI);
        if (directProperty != null) {
            // direct property found
            return directProperty;
        }

        InferredAndUndefinedDomainProperties infAndUndefDomainProps = getInferredAndUndefinedDomainProperties();

        // Then try to find property, which inferred the domain from its super properties and contains objectURI
        // in its range
        String inferredDomainProperty = findPropertyWithInferredDomainWithObjectInRange(
                subjectIRI, objectIRI, infAndUndefDomainProps.getPropertiesWithInferredDomains()
        );
        if (inferredDomainProperty != null) {
            // property found
            return inferredDomainProperty;
        }

        // and finally try to find property, whose domain is not defined, but has objectURI in its range
        /*String noDomainProperty = findPropertyWithUndefinedDomainWithObjectInRange(
                objectIRI, infAndUndefDomainProps.getPropertiesWithUndefinedDomains()
        );
        if (noDomainProperty != null) {
            // property found
            return noDomainProperty;
        }*/

        // no such property found
        return null;
    }

    /**
     * Try to find object property, which has subjectUri in its direct domain and
     * objectUri in its direct range.
     * @param subjectIRIStr
     * @param objectIRIStr
     * @return
     */
    public String findDirectPropertyForSubjectObject(String subjectIRIStr, String objectIRIStr) {
        IRI subjectIRI = valueFactory.createIRI(subjectIRIStr);
        IRI objectIRI = valueFactory.createIRI(objectIRIStr);
        Set<Resource> props = getAllProperties();
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

    public String findPropertyWithInferredDomainWithObjectInRange(String subjectIRIStr, String objectIRIStr,
                                                                  Set<PropertyWithDomain> inferredDomainProps) {

        // keep just properties, which have subjectIRI in their domain
        Set<PropertyWithDomain> filteredProperties = inferredDomainProps
            .stream()
            .filter(p -> p.getDomainIRIs().contains(subjectIRIStr))
            .collect(Collectors.toSet());

        IRI objectIRI = this.valueFactory.createIRI(objectIRIStr);
        for (PropertyWithDomain pwd : filteredProperties) {
            Model model = ontologyModel.filter(this.valueFactory.createIRI(pwd.propertyIRI), RDFS.RANGE, objectIRI);
            if (!model.isEmpty()) {
                return pwd.propertyIRI;
            }
        }
        return null;
    }

    public String findPropertyWithUndefinedDomainWithObjectInRange(String objectIRIStr,
                                                                   Set<PropertyWithDomain> noDomainProps) {

        IRI objectIRI = this.valueFactory.createIRI(objectIRIStr);
        for (PropertyWithDomain pwd : noDomainProps) {
            Model model = ontologyModel.filter(this.valueFactory.createIRI(pwd.propertyIRI), RDFS.RANGE, objectIRI);
            if (!model.isEmpty()) {
                return pwd.propertyIRI;
            }
        }
        return null;
    }

    private InferredAndUndefinedDomainProperties getInferredAndUndefinedDomainProperties() {
        // THIS action may take long time for big ontologies, as it will cascadely infer domains of properties from their
        // super properties
        Set<PropertyWithDomain> propsWithInferredDomains = new HashSet<>();
        Set<PropertyWithDomain> propsWithUndefinedDomains = new HashSet<>();

        for (Map.Entry<String, PropertyWithDomain> entry : this.propertyModel.entrySet()) {
            PropertyWithDomain prop = entry.getValue();
            // we are interested only in properties, which do not have directly defined domains
            if (prop.getDirectDomainIRIs().isEmpty()) {
                Set<String> inferredDomainIRIs = prop.getInferredDomainIRIs();
                // skip properties with invalid domains
                if (inferredDomainIRIs != null) {
                    if (!inferredDomainIRIs.isEmpty()) {
                        propsWithInferredDomains.add(prop);
                    } else {
                        propsWithUndefinedDomains.add(prop);
                    }
                }
            }
        }
        return new InferredAndUndefinedDomainProperties(propsWithInferredDomains, propsWithUndefinedDomains);
    }

    private Set<Resource> getAllProperties() {
        Set<Resource> rdfProperties = ontologyModel.filter(null, RDF.TYPE, RDF.PROPERTY).subjects();
        Set<Resource> objectProperties = ontologyModel.filter(null, RDF.TYPE, OWL.OBJECTPROPERTY).subjects();
        Set<Resource> dataProperties = ontologyModel.filter(null, RDF.TYPE, OWL.DATATYPEPROPERTY).subjects();

        return mergeResourceSets(rdfProperties, objectProperties, dataProperties);
    }

    private Set<Resource> mergeResourceSets(Set<Resource>... resourceSets) {
        Set<Resource> mergedProperties = new HashSet<>();
        for (Set<Resource> resouceSet : resourceSets) {
            mergedProperties.addAll(resouceSet);
        }
        return mergedProperties;
    }
}

class PropertyWithDomain {

    final String propertyIRI;
    final Set<PropertyWithDomain> superProperties;
    final Set<String> directDomainIRIs;

    boolean domainInferred = false;
    Set<String> inferredDomainIRIs = null;

    public PropertyWithDomain(String propertyIRI) {
        this.propertyIRI = propertyIRI;
        this.superProperties = new HashSet<>();
        this.directDomainIRIs = new HashSet<>();
    }

    public PropertyWithDomain(String propertyIRI, Set<PropertyWithDomain> superProperties, Set<String> directDomainIRIs) {
        this.propertyIRI = propertyIRI;
        this.superProperties = superProperties;
        this.directDomainIRIs = directDomainIRIs;
    }

    public PropertyWithDomain withSuperPropertiesAndDirectDomain(Set<PropertyWithDomain> superProperties, Set<String> directDomainUris) {
        return new PropertyWithDomain(
                propertyIRI,
            superProperties,
            directDomainUris
        );
    }

    public Set<String> getDomainIRIs() {
        if (!this.directDomainIRIs.isEmpty()) {
            return this.directDomainIRIs;
        } else {
            return this.getInferredDomainIRIs();
        }
    }

    public Set<String> getDirectDomainIRIs() {
        return directDomainIRIs;
    }

    public Set<String> getInferredDomainIRIs() {
        if (!this.domainInferred) {
            this.inferDomain();
        }
        return this.inferredDomainIRIs;
    }

    public void inferDomain() {

        // retrieve domains of all super properties
        Set<Set<String>> domainsOfAllSuperProps = superProperties
                .stream()
                .map(PropertyWithDomain::getDomainIRIs)
                .collect(Collectors.toSet());

        if (!domainsOfAllSuperProps.isEmpty()) {
            // do their intersection
            boolean isValid = true;
            boolean constraintsFound = false;
            Set<String> intersection = new HashSet<>();
            for (Set<String> superPropDom : domainsOfAllSuperProps) {
                if (superPropDom != null) {
                    // if the super property domain is empty, it means there are no constraints to the domain, so
                    // it can be ignored
                    if (!superPropDom.isEmpty()) {
                        if (!constraintsFound) {
                            constraintsFound = true;
                        }
                        if (intersection.isEmpty()) {
                            // if intersection was empty (no restrictions for result domain),
                            // replace it with current restrictions
                            intersection = superPropDom;
                        } else {
                            // if there already is some restrictions, so make an intersection
                            // of existing and newly discovered restrictions
                            intersection.retainAll(superPropDom);
                        }
                    }
                } else {
                    // if any of the superProp domains is null, that means the superProperty has conflicting definition
                    // and thus this property has conflicting definition as well
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                // restrictions found and not conflicting
                if (constraintsFound && (!intersection.isEmpty())) {
                    this.inferredDomainIRIs = intersection;
                } else {
                    // no restrictions found
                    if (!constraintsFound) {
                        this.inferredDomainIRIs = new HashSet<>();
                    }
                }
            }
        } else {
            // no domain inferred from superProps, so the domain
            // of this props is not restricted
            this.inferredDomainIRIs = new HashSet<>();
        }
        this.domainInferred = true;
    }
}

class InferredAndUndefinedDomainProperties {
    final Set<PropertyWithDomain> propertiesWithInferredDomains;
    final Set<PropertyWithDomain> propertiesWithUndefinedDomains;

    public InferredAndUndefinedDomainProperties(Set<PropertyWithDomain> propertiesWithInferredDomains,
                                                Set<PropertyWithDomain> propertiesWithUndefinedDomains) {
        this.propertiesWithInferredDomains = propertiesWithInferredDomains;
        this.propertiesWithUndefinedDomains = propertiesWithUndefinedDomains;
    }

    public Set<PropertyWithDomain> getPropertiesWithInferredDomains() {
        return propertiesWithInferredDomains;
    }

    public Set<PropertyWithDomain> getPropertiesWithUndefinedDomains() {
        return propertiesWithUndefinedDomains;
    }
}