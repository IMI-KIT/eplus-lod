package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.Item;
import eu.dareed.eplus.model.eso.ESO;
import eu.dareed.eplus.parsers.eso.ESOParser;
import eu.dareed.rdfmapper.xml.nodes.Mapping;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static eu.dareed.eplus.lod.Tests.loadMapping;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class ObservationResolverTest {
    private static ItemProcessor itemProcessor;
    private static ESO output;

    @BeforeClass
    public static void setup() throws JAXBException, IOException {
        Mapping unitsMapping = loadMapping("/mappings/Units.xml");
        Mapping resourcesMapping = loadMapping("/mappings/Resources.xml");
        Mapping propertiesMapping = loadMapping("/mappings/Properties.xml");
        Mapping observationsMapping = loadMapping("/mappings/Observation.xml");

        ESO output;
        try (InputStream esoStream = ObservationResolverTest.class.getResourceAsStream("/fixtures/outputs/eplusout.eso")) {
            output = new ESOParser().parseFile(esoStream);
        }

        ObservationResolverTest.itemProcessor = new ItemProcessor(resourcesMapping, propertiesMapping, unitsMapping, observationsMapping);
        ObservationResolverTest.output = output;
    }

    @Test
    public void testResolveNamedPropertyVariable() {
        Item dataDictionaryItem = output.getDataDictionary().get(8);
        Optional<ObservationMapping> result = itemProcessor.processItem(dataDictionaryItem);

        Assert.assertTrue(result.isPresent());
        ObservationResolver model = result.get().createObservationResolver(dataDictionaryItem);
        Assert.assertEquals("Electricity", model.resolveNamedVariable("property"));
    }

    @Test
    public void testResolvePropertyVariableByIndex() {
        Item dataDictionaryItem = output.getDataDictionary().get(8);
        Optional<ObservationMapping> result = itemProcessor.processItem(dataDictionaryItem);

        Assert.assertTrue(result.isPresent());

        ObservationResolver model = result.get().createObservationResolver(dataDictionaryItem);
        Assert.assertEquals("8", model.resolveIndex(0));
    }
}
