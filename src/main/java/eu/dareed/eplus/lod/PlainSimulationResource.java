package eu.dareed.eplus.lod;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public abstract class PlainSimulationResource implements SimulationResource {
    String url;
    List<String> types;

    public PlainSimulationResource(String url, List<String> types) {
        this.url = url;
        this.types = types != null ? types : Collections.emptyList();
    }

    @Override
    public List<Statement> describe(Model model) {
        Resource subject = getResource(model);
        Property predicate = RDF.type;

        List<Statement> result = new ArrayList<>(types.size());
        for (String typeURL : this.types) {
            Resource object = model.getResource(typeURL);

            result.add(model.createStatement(subject, predicate, object));
        }

        return result;
    }

    @Override
    public Resource getResource(Model model) {
        return model.getResource(url);
    }
}
