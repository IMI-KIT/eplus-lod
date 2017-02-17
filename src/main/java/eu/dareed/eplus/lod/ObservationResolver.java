package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.Field;
import eu.dareed.eplus.model.Item;
import eu.dareed.rdfmapper.VariableResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class ObservationResolver implements VariableResolver {
    final Item dataDictionaryItem;

    Map<String, String> variableMappings;

    public ObservationResolver(Item dataDictionaryItem) {
        this.dataDictionaryItem = dataDictionaryItem;
        this.variableMappings = new HashMap<>();

        variableMappings = new HashMap<>();
    }

    @Override
    public String resolveNamedVariable(String variableName) {
        return variableMappings.containsKey(variableName) ? variableMappings.get(variableName) : null;
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
