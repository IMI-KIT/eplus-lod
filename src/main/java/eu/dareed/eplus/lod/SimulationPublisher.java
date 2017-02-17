package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.Item;
import eu.dareed.eplus.model.eso.ESO;
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

        NamespaceResolver resolver = initializeNamespaceResolver();

        // TODO: Inject simulationId
        Environment baseEnvironment = new Environment(resolver);

        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefixes(resolver.getNamespaceMap());

        Map<Integer, ObservationMapping> dataDictionaryMappings = collectDataDictionaryMappings(simulationOutput);

        List<Item> dataDictionary = simulationOutput.getDataDictionary();
        for (Integer itemIndex : dataDictionaryMappings.keySet()) {
            Item dataDictionaryItem = dataDictionary.get(itemIndex);

            ObservationMapping mapping = dataDictionaryMappings.get(itemIndex);
            VariableResolver observationResolver = new ObservationEnvironment(resolver, mapping, mapping.createObservationResolver(dataDictionaryItem));

            Environment observationEnvironment = baseEnvironment.augment(observationResolver);

            model.add(mapping.describeObservation(observationEnvironment));
        }

        return model;
    }

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
