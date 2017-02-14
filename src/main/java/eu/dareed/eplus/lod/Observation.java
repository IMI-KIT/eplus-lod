package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.Field;
import eu.dareed.eplus.model.Item;
import eu.dareed.rdfmapper.VariableResolver;
import eu.dareed.rdfmapper.xml.nodes.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class Observation implements VariableResolver {
    Item dataDictionaryItem;

    Entity observation;
    Entity variable;

    Entity resource;
    Entity property;

    Entity unit;

    private Map<String, Supplier<String>> variables;

    public Observation() {
        variables = new HashMap<>();
        variables.put("property", () -> this.property.getName());
        variables.put("resource", () -> this.resource.getName());
        variables.put("unit", () -> this.resource.getName());
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
