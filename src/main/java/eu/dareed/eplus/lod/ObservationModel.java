package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.Field;
import eu.dareed.eplus.model.Item;
import eu.dareed.rdfmapper.VariableResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class ObservationModel implements VariableResolver {
    final ObservationMapping observationMapping;
    final Item dataDictionaryItem;

    private Map<String, Supplier<String>> variables;

    public ObservationModel(ObservationMapping observationMapping, Item dataDictionaryItem) {
        this.observationMapping = observationMapping;
        this.dataDictionaryItem = dataDictionaryItem;

        variables = new HashMap<>();
        variables.put("property", () -> observationMapping.property.getName());
        variables.put("resource", () -> observationMapping.resource.getName());
        variables.put("unit", () -> observationMapping.unit.getName());
        variables.put("variableId", () -> Integer.toString(this.dataDictionaryItem.firstField().integerValue()));
    }

    @Override
    public String resolveNamedVariable(String variableName) {
        return variables.containsKey(variableName) ? variables.get(variableName).get() : null;
    }

    @Override
    public String resolveIndex(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Index cannot be negative");
        }

        List<? extends Field> fields = dataDictionaryItem.getFields();
        return index < fields.size() ? fields.get(index).stringValue() : null;
    }
}
