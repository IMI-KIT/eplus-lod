package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.eso.ESO;
import eu.dareed.rdfmapper.NamespaceResolver;
import eu.dareed.rdfmapper.xml.nodes.Mapping;
import eu.dareed.rdfmapper.xml.nodes.Namespace;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class SimulationPublisher {
    private static final Logger log = LoggerFactory.getLogger(SimulationPublisher.class);

    Mapping observationMapping;
    Mapping resourcesMapping;
    Mapping propertiesMapping;
    Mapping unitsMapping;

    DictionaryLineParser lineParser;

    SimulationPublisher(Mapping observationMapping, Mapping resourcesMapping, Mapping propertiesMapping, Mapping unitsMapping) {
        this.observationMapping = observationMapping;
        this.resourcesMapping = resourcesMapping;
        this.propertiesMapping = propertiesMapping;
        this.unitsMapping = unitsMapping;

        this.lineParser = new DictionaryLineParser();
    }

    public Model createModel(ESO simulationOutput) {
        NamespaceResolver resolver = initializeNamespaceResolver();

        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefixes(resolver.getNamespaceMap());
        // Map those outputs

        return model;
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
