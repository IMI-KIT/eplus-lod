package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.eso.ESO;
import eu.dareed.eplus.parsers.eso.ESOParser;
import eu.dareed.rdfmapper.MappingIO;
import eu.dareed.rdfmapper.xml.nodes.Mapping;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class Tests {
    static Mapping loadMapping(String path) throws IOException, JAXBException {
        try (InputStream inputStream = ItemProcessorTest.class.getResourceAsStream(path)) {
            return new MappingIO().loadXML(inputStream);
        }
    }

    static ESO loadSimulationOutput(String path) throws IOException {
        try (InputStream esoStream = Tests.class.getResourceAsStream("/fixtures/outputs/eplusout_trunc.eso")) {
            return new ESOParser().parseFile(esoStream);
        }
    }
}
