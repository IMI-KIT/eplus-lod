package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.Item;
import eu.dareed.eplus.model.eso.ESO;
import eu.dareed.eplus.parsers.eso.ESOParser;
import eu.dareed.rdfmapper.MappingIO;
import eu.dareed.rdfmapper.xml.nodes.Mapping;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class ItemProcessorTest {
    private static ItemProcessor itemProcessor;
    private static ESO output;

    @BeforeClass
    public static void setup() throws JAXBException, IOException {
        Mapping unitsMapping = loadMapping("/mappings/Units.xml");
        Mapping resourcesMapping = loadMapping("/mappings/Resources.xml");
        Mapping propertiesMapping = loadMapping("/mappings/Properties.xml");
        Mapping observationsMapping = loadMapping("/mappings/Observation.xml");

        ESO output;
        try (InputStream esoStream = ItemProcessorTest.class.getResourceAsStream("/fixtures/outputs/eplusout.eso")) {
            output = new ESOParser().parseFile(esoStream);
        }

        ItemProcessorTest.itemProcessor = new ItemProcessor(resourcesMapping, propertiesMapping, unitsMapping, observationsMapping);
        ItemProcessorTest.output = output;
    }

    @Test
    public void testGetValidObservation() {
        Optional<ObservationMapping> result = itemProcessor.processItem(output.getDataDictionary().get(8));
        Assert.assertTrue(result.isPresent());

        ObservationMapping observation = result.get();
        Assert.assertEquals("J", observation.unit.getName());
        Assert.assertEquals("Electricity", observation.property.getName());
        Assert.assertEquals("Facility", observation.resource.getName());
    }

    @Test
    public void testGetInvalidObservation() {
        Optional<ObservationMapping> result = itemProcessor.processItem(output.getDataDictionary().get(7));
        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void testResolveNamedPropertyVariable() {
        Item dataDictionaryItem = output.getDataDictionary().get(8);
        Optional<ObservationMapping> result = itemProcessor.processItem(dataDictionaryItem);

        Assert.assertTrue(result.isPresent());
        ObservationModel model = new ObservationModel(result.get(), dataDictionaryItem);
        Assert.assertEquals("Electricity", model.resolveNamedVariable("property"));
    }

    @Test
    public void testResolvePropertyVariableByIndex() {
        Item dataDictionaryItem = output.getDataDictionary().get(8);
        Optional<ObservationMapping> result = itemProcessor.processItem(dataDictionaryItem);

        Assert.assertTrue(result.isPresent());

        ObservationModel model = new ObservationModel(result.get(), dataDictionaryItem);
        Assert.assertEquals("8", model.resolveIndex(0));
    }

    private static Mapping loadMapping(String path) throws IOException, JAXBException {
        try (InputStream inputStream = ItemProcessorTest.class.getResourceAsStream(path)) {
            return new MappingIO().loadXML(inputStream);
        }
    }
}
