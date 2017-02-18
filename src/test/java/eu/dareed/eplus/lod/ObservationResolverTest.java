package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.Item;
import eu.dareed.eplus.model.eso.ESO;
import eu.dareed.rdfmapper.Context;
import eu.dareed.rdfmapper.VariableResolver;
import eu.dareed.rdfmapper.xml.nodes.Mapping;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Optional;

import static eu.dareed.eplus.lod.Tests.loadMapping;
import static eu.dareed.eplus.lod.Tests.loadSimulationOutput;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class ObservationResolverTest {
    private static Context baseContext;
    private static ItemProcessor itemProcessor;
    private static ESO output;

    @BeforeClass
    public static void setup() throws JAXBException, IOException {
        Mapping unitsMapping = loadMapping("/mappings/Units.xml");
        Mapping resourcesMapping = loadMapping("/mappings/Resources.xml");
        Mapping propertiesMapping = loadMapping("/mappings/Properties.xml");
        Mapping observationsMapping = loadMapping("/mappings/Observation.xml");

        ObservationResolverTest.output = loadSimulationOutput("/fixtures/outputs/eplusout.eso");
        ObservationResolverTest.itemProcessor = new ItemProcessor(resourcesMapping, propertiesMapping, unitsMapping, observationsMapping);
        ObservationResolverTest.baseContext = new Context(new VariableMapping());
    }

    @Test
    public void testResolveNamedPropertyVariable() {
        Item dataDictionaryItem = output.getDataDictionary().get(8);
        Optional<ObservationMapping> result = itemProcessor.processItem(dataDictionaryItem);

        Assert.assertTrue(result.isPresent());
        VariableResolver model = result.get().createObservationResolver(dataDictionaryItem, baseContext);
        Assert.assertEquals("Electricity", model.resolveNamedVariable("property"));
    }

    @Test
    public void testResolvePropertyVariableByIndex() {
        Item dataDictionaryItem = output.getDataDictionary().get(8);
        Optional<ObservationMapping> result = itemProcessor.processItem(dataDictionaryItem);

        Assert.assertTrue(result.isPresent());

        VariableResolver model = result.get().createObservationResolver(dataDictionaryItem, baseContext);
        Assert.assertNull(model.resolveIndex(0));
    }
}
