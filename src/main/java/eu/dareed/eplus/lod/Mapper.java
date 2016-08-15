package eu.dareed.eplus.lod;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Maps E+ object names to the simulation resources.
 *
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public interface Mapper extends Predicate<String>, Function<String, SimulationResource> {
    Map<String, SimulationResource> getMapping();

    boolean containsResource(String name);

    SimulationResource getResource(String name);

    @Override
    default boolean test(String resource) {
        return containsResource(resource);
    }

    @Override
    default SimulationResource apply(String resource) {
        return getResource(resource);
    }
}
