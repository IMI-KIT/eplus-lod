package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.eso.ESO;
import eu.dareed.eplus.parsers.eso.ESOParser;
import eu.dareed.rdfmapper.xml.nodes.Mapping;
import org.apache.jena.rdf.model.Model;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;

import static eu.dareed.eplus.lod.Tests.loadMapping;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class SimulationPublisherTest {
    private static SimulationPublisher simulationPublisher;
    private static ESO simulationOutput;

    @BeforeClass
    public static void setup() throws IOException, JAXBException {
        Mapping unitsMapping = loadMapping("/mappings/Units.xml");
        Mapping resourcesMapping = loadMapping("/mappings/Resources.xml");
        Mapping propertiesMapping = loadMapping("/mappings/Properties.xml");
        Mapping observationsMapping = loadMapping("/mappings/Observation.xml");

        ESO output;
        try (InputStream esoStream = SimulationPublisherTest.class.getResourceAsStream("/fixtures/outputs/eplusout.eso")) {
            output = new ESOParser().parseFile(esoStream);
        }

        simulationPublisher = new SimulationPublisher(observationsMapping, resourcesMapping, propertiesMapping, unitsMapping);
        simulationOutput = output;
    }

    @Test
    public void testPublishModel() {
        Model residential = simulationPublisher.createModel(simulationOutput, "RESIDENTIAL", 69);

        residential.write(System.out, "TTL", null);
    }
}
