package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.eso.ESO;
import java.util.Map;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class SimulationPublisher {
    SimulationContext context;

    ESO output;

    Map<String, SimulationResource> resources;
    Map<String, SimulationResource> properties;
}
