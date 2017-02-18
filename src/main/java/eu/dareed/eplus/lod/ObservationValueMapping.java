package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.Item;
import eu.dareed.rdfmapper.Context;
import eu.dareed.rdfmapper.Environment;
import eu.dareed.rdfmapper.VariableResolver;
import eu.dareed.rdfmapper.xml.nodes.Entity;
import eu.dareed.rdfmapper.xml.nodes.Property;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import java.util.List;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class ObservationValueMapping {
    private final List<Entity> entityModels;

    public ObservationValueMapping(List<Entity> valueMapping) {
        this.entityModels = valueMapping;
    }

    VariableResolver createObservationValueResolver(Item item, Context baseContext) {
        VariableMapping variableMapping = new VariableMapping();
        variableMapping.mapVariable("dayOfSimulation", item.getField(1).stringValue());
        variableMapping.mapVariable("month", item.getField(2).stringValue());
        variableMapping.mapVariable("day", item.getField(3).stringValue());
        variableMapping.mapVariable("hour", item.getField(5).stringValue());
        variableMapping.mapVariable("startMinute", item.getField(6).stringValue());
        variableMapping.mapVariable("endMinute", item.getField(7).stringValue());

        return baseContext.augment(variableMapping);
    }

    Model describe(Environment item) {
        Model model = ModelFactory.createDefaultModel();

        for (Entity entity : entityModels) {
            Resource subject;
            if (entity.getUri().isEmpty()) {
                subject = model.createResource();
            } else {
                subject = model.createResource(item.resolveURL(entity.getUri()));
            }

            for (String type : entity.getTypes()) {
                model.add(subject, RDF.type, model.createResource(item.resolveURL(type)));
            }

            for (Property property : entity.getProperties()) {
                model.add(property.describe(model, subject, item));
            }
        }

        return model;
    }
}
