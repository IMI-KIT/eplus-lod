package eu.dareed.eplus.lod;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public interface Output {
    /**
     * TODO: Properties
     *
     *  - environment (simulation output and input)
     *  - vocabulary
     */

    SimulationResource getFeature();
    SimulationResource getProperty();
    SimulationResource getSensor();

    SimulationResource getOutput();
    SimulationResource getOservationQuality();
}
