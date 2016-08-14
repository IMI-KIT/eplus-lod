package eu.dareed.eplus.lod;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
class OutputMetadata {
    final String name;
    final String unit;
    final ReportFrequency reportFrequency;
    final List<String> schema;

    OutputMetadata(String name, String unit, ReportFrequency reportFrequency, List<String> schema) {
        this.name = name;
        this.unit = unit;
        this.reportFrequency = reportFrequency;
        this.schema = schema != null ? schema : Collections.emptyList();
    }
}
