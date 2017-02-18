package eu.dareed.eplus.lod;

import eu.dareed.rdfmapper.VariableResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class VariableMapping implements VariableResolver {
    private final Map<String, String> variableMappings;

    public VariableMapping() {
        this.variableMappings = new HashMap<>();
    }

    void mapVariable(String name, String value) {
        variableMappings.put(name, value);
    }

    @Override
    public String resolveNamedVariable(String variableName) {
        return variableMappings.get(variableName);
    }

    @Override
    public String resolveIndex(int index) {
        return null;
    }
}
