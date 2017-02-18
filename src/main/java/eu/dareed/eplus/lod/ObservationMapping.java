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

import java.util.List;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
class ObservationMapping {
    List<Entity> observations;
    List<Entity> observationResults;

    List<Entity> sensors;
    List<Entity> resources;
    List<Entity> properties;

    List<Entity> units;

    ObservationMapping() {
    }

    VariableResolver createObservationResolver(Item dataDictionaryItem, Context baseContext) {
        VariableMapping variableMapping = new VariableMapping();
        variableMapping.mapVariable("property", properties.get(0).getName());
        variableMapping.mapVariable("resource", resources.get(0).getName());
        variableMapping.mapVariable("unit", units.get(0).getName());
        variableMapping.mapVariable("variableId", Integer.toString(dataDictionaryItem.firstField().integerValue()));

        return baseContext.augment(variableMapping);
    }

    public Model describeObservation(Environment dataDictionaryItemEnvironment) {
        Model result = ModelFactory.createDefaultModel();

        result.add(describe(observations, dataDictionaryItemEnvironment));
        result.add(describe(observationResults, dataDictionaryItemEnvironment));
        result.add(describe(sensors, dataDictionaryItemEnvironment));
        result.add(describe(properties, dataDictionaryItemEnvironment));
        result.add(describe(resources, dataDictionaryItemEnvironment));

        return result;
    }

    private Model describe(List<Entity> entityModels, Environment environment) {
        Model model = ModelFactory.createDefaultModel();

        for (Entity entity : entityModels) {
            String subjectURL = environment.resolveURL(entity.getUri());

            for (String type : entity.getTypes()) {
                model.add(model.createStatement(model.createResource(subjectURL),
                        RDF.type, model.createResource(environment.resolveURL(type))));
            }

            for (Property property : entity.getProperties()) {
                model.add(property.describe(subjectURL, environment));
            }

        }

        return model;
    }
}
