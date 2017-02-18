package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.Item;
import eu.dareed.eplus.model.eso.DataPoints;
import eu.dareed.eplus.model.eso.ESO;
import eu.dareed.eplus.model.eso.ESOItem;
import eu.dareed.rdfmapper.Environment;
import eu.dareed.rdfmapper.NamespaceResolver;
import eu.dareed.rdfmapper.VariableResolver;
import eu.dareed.rdfmapper.xml.nodes.Mapping;
import eu.dareed.rdfmapper.xml.nodes.Namespace;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class SimulationPublisher {
    private static final Logger log = LoggerFactory.getLogger(SimulationPublisher.class);

    Mapping observationMapping;
    Mapping resourcesMapping;
    Mapping propertiesMapping;
    Mapping unitsMapping;

    ItemProcessor itemProcessor;

    SimulationPublisher(Mapping observationMapping, Mapping resourcesMapping, Mapping propertiesMapping, Mapping unitsMapping) {
        this.observationMapping = observationMapping;
        this.resourcesMapping = resourcesMapping;
        this.propertiesMapping = propertiesMapping;
        this.unitsMapping = unitsMapping;

        this.itemProcessor = new ItemProcessor(resourcesMapping, propertiesMapping, unitsMapping, observationMapping);
    }

    public Model createModel(ESO simulationOutput, String environmentName, long simulationId) {
        if (!simulationOutput.getEnvironments().containsKey(environmentName)) {
            return ModelFactory.createDefaultModel();
        }

        VariableMapping variableMapping = new VariableMapping();
        variableMapping.mapVariable("simulationId", Long.toString(simulationId));

        NamespaceResolver namespaceResolver = initializeNamespaceResolver();
        Environment baseEnvironment = new Environment(namespaceResolver, variableMapping);

        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefixes(namespaceResolver.getNamespaceMap());

        Map<Integer, ObservationMapping> dataDictionaryMappings = collectDataDictionaryMappings(simulationOutput);

        List<Item> dataDictionary = simulationOutput.getDataDictionary();
        Map<Integer, Environment> observationEnvironments = new HashMap<>(dataDictionaryMappings.size());
        for (Integer itemIndex : dataDictionaryMappings.keySet()) {
            Item dataDictionaryItem = dataDictionary.get(itemIndex);
            ObservationMapping observationMapping = dataDictionaryMappings.get(itemIndex);

            VariableResolver observationResolver = observationMapping.createObservationResolver(dataDictionaryItem, baseEnvironment.getContext());

            Environment observationEnvironment = baseEnvironment.augment(observationResolver);

            observationEnvironment = observationEnvironment.augment(
                    new ObservationEnvironment(namespaceResolver, observationEnvironment.getContext(), observationMapping));

            model.add(observationMapping.describeObservation(observationEnvironment));

            observationEnvironments.put(dataDictionaryItem.firstField().integerValue(), observationEnvironment);
        }

        ObservationValueMapping valueMapping = new ObservationValueMapping(itemProcessor.observationMapping.get(ItemProcessor.OBSERVATION_VALUE));
        for (DataPoints dataPoints : simulationOutput.getDataPoints(environmentName)) {
            Item dataPointsItem = dataPoints.getItem();
            if (dataPointsItem.getFields().size() < 9) {
                continue;
            }


            for (ESOItem value : dataPoints.getData()) {
                if (!observationEnvironments.containsKey(value.getDictionaryItem().firstField().integerValue())) {
                    continue;
                }

                Environment observationEnvironment = observationEnvironments.get(value.getDictionaryItem().firstField().integerValue());

                ItemResolver itemResolver = new ItemResolver(value);
                VariableResolver outputMetadata = valueMapping.createObservationValueResolver(dataPointsItem, observationEnvironment.getContext());

                Environment valueEnvironment = observationEnvironment.augment(itemResolver).augment(outputMetadata);

                model.add(valueMapping.describe(valueEnvironment));
            }
        }

        return model;
    }

    /**
     * Maps data dictionary items' indexes to simulation output mappings.
     *
     * @param simulationOutput the output containing the data dictionary.
     * @return a map of {@link ObservationMapping}s
     */
    protected Map<Integer, ObservationMapping> collectDataDictionaryMappings(ESO simulationOutput) {
        Map<Integer, ObservationMapping> items = new HashMap<>();

        List<Item> dataDictionary = simulationOutput.getDataDictionary();
        for (int dataDictionaryItem = 0; dataDictionaryItem < dataDictionary.size(); dataDictionaryItem++) {
            Optional<ObservationMapping> mapping = itemProcessor.processItem(dataDictionary.get(dataDictionaryItem));

            if (mapping.isPresent()) {
                items.put(dataDictionaryItem, mapping.get());
            }
        }

        return items;
    }

    protected NamespaceResolver initializeNamespaceResolver() {
        Map<String, String> namespaces = new HashMap<>();

        namespaces.putAll(obtainNamespacesMapFromMapping(observationMapping));
        namespaces.putAll(obtainNamespacesMapFromMapping(resourcesMapping));
        namespaces.putAll(obtainNamespacesMapFromMapping(propertiesMapping));
        namespaces.putAll(obtainNamespacesMapFromMapping(unitsMapping));

        return new NamespaceResolver(namespaces);
    }

    private Map<String, String> obtainNamespacesMapFromMapping(Mapping mapping) {
        if (mapping == null) {
            return Collections.emptyMap();
        } else {
            List<Namespace> namespaces = mapping.getNamespaces();
            Map<String, String> namespaceMapping = new HashMap<>(namespaces.size());

            for (Namespace namespace : namespaces) {
                namespaceMapping.put(namespace.getPrefix(), namespace.getUri());
            }

            return namespaceMapping;
        }
    }
}
