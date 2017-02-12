package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.Item;
import eu.dareed.rdfmapper.VariableResolver;
import eu.dareed.rdfmapper.xml.nodes.Entity;

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

    @Override
    public String resolveNamedVariable(String variableName) {
        return null;
    }

    @Override
    public String resolveIndex(int index) {
        return null;
    }
}
