package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.eso.ESO;
import eu.dareed.rdfmapper.xml.nodes.Mapping;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;

import static eu.dareed.eplus.lod.Tests.loadMapping;
import static eu.dareed.eplus.lod.Tests.loadSimulationOutput;

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

        simulationOutput = loadSimulationOutput("/fixtures/outputs/eplusout_trunc.eso");
        simulationPublisher = new SimulationPublisher(observationsMapping, resourcesMapping, propertiesMapping, unitsMapping);
    }

    @Test
    public void testPublishModel() throws IOException {
        Model residential = simulationPublisher.createModel(simulationOutput, "RESIDENTIAL", 69);

        Assert.assertTrue(residential.contains(residential.createResource("http://dareed.eu/resources/69/observations/38/Electricity"), RDF.type));
        Assert.assertTrue(residential.contains(residential.createResource("http://dareed.eu/resources/69/observations/18/Electricity"), RDF.type));
        Assert.assertTrue(residential.contains(residential.createResource("http://dareed.eu/resources/69/observations/591/CO2"), RDF.type));
    }
}
