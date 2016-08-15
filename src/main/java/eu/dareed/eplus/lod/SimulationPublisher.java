package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.Field;
import eu.dareed.eplus.model.Item;
import eu.dareed.eplus.model.eso.ESO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class SimulationPublisher {
    private static final Logger log = LoggerFactory.getLogger(SimulationPublisher.class);

    SimulationContext context;

    ESO output;

    Mapper resources;
    Mapper properties;
    Mapper units;

    DictionaryLineParser lineParser;

    SimulationPublisher() {
        this.lineParser = new DictionaryLineParser();
    }

    List<OutputPublisher> selectOutputs() {
        Function<Item, Optional<OutputPublisher>> selector = item -> {
            List<? extends Field> fields = item.getFields();
            String outputMetadataLiteral = fields.get(fields.size() - 1).stringValue();

            if (!lineParser.test(outputMetadataLiteral)) {
                return Optional.empty();
            }

            OutputMetadata metadata = lineParser.apply(outputMetadataLiteral);
            String[] tokens = metadata.name.split(":");

            if (tokens.length > 1) {
                int propertyIndex = matchResource(properties, tokens);
                int resourceIndex = matchResource(resources, tokens);

                if (propertyIndex + resourceIndex <= 0) {
                    return Optional.empty();
                }

                if (!units.containsResource(metadata.unit)) {
                    return Optional.empty();
                }

                SimulationResource property = properties.getResource(tokens[propertyIndex]);
                SimulationResource resource = resources.getResource(tokens[resourceIndex]);
                SimulationResource unit = units.getResource(metadata.unit);

                return Optional.of(
                        new NaiiveOutputPublisher(
                                metadata,
                                unit,
                                resource,
                                property)
                );
            } else {
                return Optional.empty();
            }
        };

        return selectReportItems().stream()
                .map(selector)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<Item> selectReportItems() {
        return output.getDataDictionary().stream()
                .filter(item -> item.getField(1).integerValue() > 5)
                .collect(Collectors.toList());
    }

    private int matchResource(Mapper mapper, String[] outputName) {
        if (mapper.containsResource(outputName[0])) {
            return 0;
        } else if (mapper.containsResource(outputName[1])) {
            return 1;
        } else {
            return 0;
        }
    }
}
