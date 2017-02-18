package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.Item;
import eu.dareed.rdfmapper.Context;
import eu.dareed.rdfmapper.Environment;
import eu.dareed.rdfmapper.VariableResolver;
import eu.dareed.rdfmapper.xml.nodes.Entity;
import eu.dareed.rdfmapper.xml.nodes.Property;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
class ObservationMapping {
    Entity observation;
    Entity observationResult;

    Entity sensor;
    Entity resource;
    Entity property;

    Entity unit;

    ObservationMapping() {
    }

    VariableResolver createObservationResolver(Item dataDictionaryItem, Context baseContext) {
        ObservationResolver observationResolver = new ObservationResolver(dataDictionaryItem);

        VariableMapping variableMapping = new VariableMapping();
        variableMapping.mapVariable("property", property.getName());
        variableMapping.mapVariable("resource", resource.getName());
        variableMapping.mapVariable("unit", unit.getName());
        variableMapping.mapVariable("variableId", Integer.toString(dataDictionaryItem.firstField().integerValue()));

        return baseContext.augment(variableMapping);
    }

    public Model describeObservation(Environment dataDictionaryItemEnvironment) {
        Model result = ModelFactory.createDefaultModel();

        result.add(describe(observation, dataDictionaryItemEnvironment));
        result.add(describe(observationResult, dataDictionaryItemEnvironment));
        result.add(describe(sensor, dataDictionaryItemEnvironment));
        result.add(describe(property, dataDictionaryItemEnvironment));
        result.add(describe(resource, dataDictionaryItemEnvironment));

        return result;
    }

    private Model describe(Entity entityModel, Environment environment) {
        if (entityModel == null) {
            return ModelFactory.createDefaultModel();
        }

        Model model = ModelFactory.createDefaultModel();

        String subjectURL = environment.resolveURL(entityModel.getUri());

        for (String type : entityModel.getTypes()) {
            model.add(model.createStatement(model.createResource(subjectURL),
                    RDF.type, model.createResource(environment.resolveURL(type))));
        }

        for (Property property : entityModel.getProperties()) {
            model.add(property.describe(subjectURL, environment));
        }

        return model;
    }
}
