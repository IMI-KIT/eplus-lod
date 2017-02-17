package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.Item;
import eu.dareed.rdfmapper.xml.nodes.Entity;

import java.util.Map;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
class ObservationMapping {
    Entity observation;
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
}
