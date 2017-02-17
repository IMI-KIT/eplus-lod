package eu.dareed.eplus.lod;

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
}
