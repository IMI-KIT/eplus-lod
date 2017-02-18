package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.Field;
import eu.dareed.eplus.model.Item;
import eu.dareed.rdfmapper.xml.nodes.Entity;
import eu.dareed.rdfmapper.xml.nodes.Mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class ItemProcessor {
    public static final String OBSERVATION = "Observation";
    public static final String SENSOR = "Sensor";
    public static final String OBSERVATION_RESULT = "ObservationResult";
    public static final String OBSERVATION_VALUE = "ObservationValue";

    final Map<String, Entity> resources;
    final Map<String, Entity> properties;
    final Map<String, Entity> units;
    final Map<String, Entity> observationMapping;

    private final DictionaryLineParser lineParser;

    ItemProcessor(Mapping resourcesMapping, Mapping properties, Mapping units, Mapping observationMapping) {
        this.resources = initializeMap(resourcesMapping);
        this.properties = initializeMap(properties);
        this.units = initializeMap(units);
        this.observationMapping = initializeMap(observationMapping);

        this.lineParser = new DictionaryLineParser();

        if (!(this.observationMapping.containsKey(OBSERVATION)
                && this.observationMapping.containsKey(SENSOR))
                && this.observationMapping.containsKey(OBSERVATION_RESULT)
                && this.observationMapping.containsKey(OBSERVATION_VALUE)) {
            throw new IllegalArgumentException("Observation mapping file is invalid. It should define mappings for both the Variable & Observation entities");
        }
    }

    Optional<ObservationMapping> processItem(Item item) {
        List<? extends Field> fields = item.getFields();
        String outputMetadataLiteral = fields.get(fields.size() - 1).stringValue();

        if (!lineParser.test(outputMetadataLiteral)) {
            // log metadata not parsable
            return Optional.empty();
        }

        OutputMetadata metadata = lineParser.apply(outputMetadataLiteral);

        if (metadata.hasCompoundName()) {
            String[] tokensInOutputName = metadata.name.split(":");
            int propertyIndex = matchProperty(tokensInOutputName);
            int resourceIndex = matchResource(tokensInOutputName);

            if (propertyIndex + resourceIndex <= 0) {
                // log property & resource not found
                return Optional.empty();
            }

            if (!units.containsKey(metadata.unit)) {
                // log unit not found.
                return Optional.empty();
            }

            ObservationMapping observation = new ObservationMapping();
            observation.observation = observationMapping.get(OBSERVATION);
            observation.sensor = observationMapping.get(SENSOR);
            observation.observationResult = observationMapping.get(OBSERVATION_RESULT);

            observation.unit = units.get(metadata.unit);
            observation.resource = resources.get(tokensInOutputName[resourceIndex]);
            observation.property = properties.get(tokensInOutputName[propertyIndex]);

            return Optional.of(observation);
        } else {
            // log name not parsable
            return Optional.empty();
        }
    }

    private int matchResource(String[] outputName) {
        if (resources.containsKey(outputName[0])) {
            return 0;
        } else if (resources.containsKey(outputName[1])) {
            return 1;
        } else {
            return -1;
        }
    }

    private int matchProperty(String[] outputName) {
        if (properties.containsKey(outputName[0])) {
            return 0;
        } else if (properties.containsKey(outputName[1])) {
            return 1;
        } else {
            return -1;
        }
    }

    private Map<String, Entity> initializeMap(Mapping mapping) {
        List<Entity> entities = mapping.getEntities();

        Map<String, Entity> result = new HashMap<>(entities.size());
        for (Entity entity : entities) {
            result.put(entity.getName(), entity);
        }
        return result;
    }
}
