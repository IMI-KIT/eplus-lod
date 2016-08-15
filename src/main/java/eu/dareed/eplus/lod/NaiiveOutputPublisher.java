package eu.dareed.eplus.lod;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class NaiiveOutputPublisher implements OutputPublisher {

    OutputMetadata outputMetadata;

    SimulationResource unit;
    SimulationResource resource;
    SimulationResource property;

    public NaiiveOutputPublisher(OutputMetadata outputMetadata, SimulationResource unit, SimulationResource resource, SimulationResource property) {
        this.outputMetadata = outputMetadata;

        this.unit = unit;
        this.resource = resource;
        this.property = property;
    }

    @Override
    public SimulationResource getFeature() {
        return null;
    }

    @Override
    public SimulationResource getProperty() {
        return null;
    }

    @Override
    public SimulationResource getSensor() {
        return null;
    }

    @Override
    public SimulationResource getOutput() {
        return null;
    }

    @Override
    public SimulationResource getObservation() {
        return null;
    }

    @Override
    public SimulationResource getObservationQuality() {
        return null;
    }
}
