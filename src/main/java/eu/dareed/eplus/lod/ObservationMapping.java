package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.Item;
import eu.dareed.rdfmapper.Environment;
import eu.dareed.rdfmapper.xml.nodes.Entity;
import eu.dareed.rdfmapper.xml.nodes.Property;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;

import java.util.Map;

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

    ObservationResolver createObservationResolver(Item dataDictionaryItem) {
        ObservationResolver observationResolver = new ObservationResolver(dataDictionaryItem);

        Map<String, String> variableMappings = observationResolver.variableMappings;
        variableMappings.put("property", property.getName());
        variableMappings.put("resource", resource.getName());
        variableMappings.put("unit", unit.getName());
        variableMappings.put("variableId", Integer.toString(dataDictionaryItem.firstField().integerValue()));

        return observationResolver;
    }

    public Model describeObservation(Environment dataDictionaryItemEnvironment) {
        Model result = ModelFactory.createDefaultModel();

        result.add(describe(observation, dataDictionaryItemEnvironment));
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

        for (String type : resource.getTypes()) {
            model.add(model.createStatement(model.createResource(subjectURL),
                    RDF.type, model.createResource(environment.resolveURL(type))));
        }

        for (Property property : entityModel.getProperties()) {
            model.add(property.describe(subjectURL, environment));
        }

        return model;
    }
}
