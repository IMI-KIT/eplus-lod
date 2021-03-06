package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.Field;
import eu.dareed.eplus.model.Item;
import eu.dareed.rdfmapper.xml.nodes.Entity;
import eu.dareed.rdfmapper.xml.nodes.Mapping;

import java.util.*;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class ItemProcessor {
    public static final String OBSERVATION = "Observation";
    public static final String SENSOR = "Sensor";
    public static final String OBSERVATION_RESULT = "ObservationResult";
    public static final String OBSERVATION_VALUE = "ObservationValue";

    final Map<String, List<Entity>> resources;
    final Map<String, List<Entity>> properties;
    final Map<String, List<Entity>> units;
    final Map<String, List<Entity>> observationMapping;

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
                // log properties & resources not found
                return Optional.empty();
            }

            if (!units.containsKey(metadata.unit)) {
                // log units not found.
                return Optional.empty();
            }

            ObservationMapping observation = new ObservationMapping();
            observation.observations = observationMapping.get(OBSERVATION);
            observation.sensors = observationMapping.get(SENSOR);
            observation.observationResults = observationMapping.get(OBSERVATION_RESULT);

            observation.units = units.get(metadata.unit);
            observation.resources = resources.get(tokensInOutputName[resourceIndex]);
            observation.properties = properties.get(tokensInOutputName[propertyIndex]);

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

    private Map<String, List<Entity>> initializeMap(Mapping mapping) {
        List<Entity> entities = mapping.getEntities();

        Map<String, List<Entity>> result = new HashMap<>(entities.size());
        for (Entity entity : entities) {
            List<Entity> entityList;
            if (result.containsKey(entity.getName())) {
                entityList = result.get(entity.getName());
            } else {
                entityList = new LinkedList<>();
                result.put(entity.getName(), entityList);
            }
            entityList.add(entity);
        }
        return result;
    }
}
