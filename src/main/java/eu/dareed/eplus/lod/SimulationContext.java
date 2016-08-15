package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.idf.IDF;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class SimulationContext {
    IDF idf;

    String id;
    String namespace;

    public SimulationContext(String id, String namespace) {
        this.id = id;
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getId() {
        return id;
    }

    public String rootUrl() {
        return namespace + "/" + id;
    }

    public String mintUrl(String... components) {
        return namespace + "/" + id + StringUtils.join(components, "/");
    }
}
