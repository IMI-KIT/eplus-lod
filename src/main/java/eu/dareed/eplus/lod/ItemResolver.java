package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.Field;
import eu.dareed.eplus.model.eso.ESOItem;
import eu.dareed.rdfmapper.VariableResolver;

import java.util.List;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class ItemResolver implements VariableResolver {
    private final List<? extends Field> itemFields;

    public ItemResolver(ESOItem item) {
        this.itemFields = item.getFields();
    }

    @Override
    public String resolveNamedVariable(String variableName) {
        return null;
    }

    @Override
    public String resolveIndex(int index) {
        return index < itemFields.size() ? itemFields.get(index).stringValue() : null;
    }
}
