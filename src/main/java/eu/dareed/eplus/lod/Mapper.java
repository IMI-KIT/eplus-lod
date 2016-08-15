package eu.dareed.eplus.lod;

import java.util.Map;

/**
 * Maps E+ object names to the simulation resources.
 *
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public interface Mapper {
    Map<String, SimulationResource> getMapping();

    boolean containsResource(String name);

    SimulationResource getResource(String name);
}
