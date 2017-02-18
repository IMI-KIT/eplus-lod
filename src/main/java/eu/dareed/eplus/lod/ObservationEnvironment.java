package eu.dareed.eplus.lod;

import eu.dareed.rdfmapper.Context;
import eu.dareed.rdfmapper.Environment;
import eu.dareed.rdfmapper.NamespaceResolver;
import eu.dareed.rdfmapper.VariableResolver;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class ObservationEnvironment extends Environment implements VariableResolver {
    private final ObservationMapping observationMapping;

    public ObservationEnvironment(NamespaceResolver namespaceResolver, Context observationContext, ObservationMapping observationMapping) {
        super(namespaceResolver, observationContext);

        this.observationMapping = observationMapping;
    }

    @Override
    public String resolveNamedVariable(String variableName) {
        switch (variableName) {
            case ("propertyURL"):
                return resolveURL(observationMapping.property.getUri());
            case "resourceURL":
                return resolveURL(observationMapping.resource.getUri());
            case "unitURL":
                return resolveURL(observationMapping.unit.getUri());
            case "sensorURL":
                return resolveURL(observationMapping.sensor.getUri());
            case "observationResultURL":
                return resolveURL(observationMapping.observationResult.getUri());
            default:
                return context.resolveNamedVariable(variableName);
        }
    }

    @Override
    public String resolveIndex(int index) {
        return context.resolveIndex(index);
    }
}
