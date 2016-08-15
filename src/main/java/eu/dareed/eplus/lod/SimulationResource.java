package eu.dareed.eplus.lod;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.List;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public interface SimulationResource {
    List<Statement> describe(Model model);

    Resource getResource(Model model);
}
