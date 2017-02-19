package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.eso.ESO;
import eu.dareed.eplus.parsers.eso.ESOParser;
import eu.dareed.rdfmapper.MappingIO;
import eu.dareed.rdfmapper.xml.nodes.Mapping;
import org.apache.commons.cli.*;
import org.apache.jena.rdf.model.Model;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class Publish {
    private static final String HEADER = "Exposes EnergyPlus simulation output files as linked open data\n\n";
    private static final String FOOTER = "\nPlease, report issues at https://github.com/IMI-KIT/eplus-lod/issues";
    private static final String CMD_LINE_SYNTAX = "publish [OPTION]...";

    private final Options options;
    private final HelpFormatter helpFormatter;
    private final MappingIO mappingIO;

    private Publish(Options options, HelpFormatter helpFormatter) {
        this.options = options;
        this.helpFormatter = helpFormatter;
        this.mappingIO = new MappingIO();
    }

    private int execute(String[] args) {
        CommandLineParser parser = new DefaultParser();

        CommandLine commandLine;

        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException exp) {
            System.err.println(exp.getMessage());
            return 1;
        }

        List<String> offenses = validateCommandLine(commandLine);
        if (!offenses.isEmpty()) {
            System.err.println("Invalid or incomplete parameters: " + offenses.iterator().next());
            helpFormatter.printHelp(
                    CMD_LINE_SYNTAX,
                    HEADER,
                    options,
                    FOOTER,
                    false);
            return 1;
        }

        if (commandLine.hasOption('h')) {
            helpFormatter.printHelp(
                    CMD_LINE_SYNTAX,
                    HEADER,
                    options,
                    FOOTER,
                    false);
            return 0;
        }

        Mapping unitsMapping;
        Mapping resourcesMapping;
        Mapping propertiesMapping;
        Mapping observationsMapping;
        try {
            unitsMapping = loadMapping(commandLine.getOptionValue('u'));
            resourcesMapping = loadMapping(commandLine.getOptionValue('r'));
            propertiesMapping = loadMapping(commandLine.getOptionValue('p'));
            observationsMapping = loadMapping(commandLine.getOptionValue('x'));
        } catch (IOException | JAXBException e) {
            System.err.println("Error parsing the mapping files: " + e.getMessage());
            return 1;
        }

        ESO input;
        try (InputStream inputStream = new FileInputStream(commandLine.getOptionValue('i'))){
            input = new ESOParser().parseFile(inputStream);
        } catch (IOException e) {
            System.err.println("Error while parsing the input file: " + e.getMessage());
            return 1;
        }

        SimulationPublisher publisher = new SimulationPublisher(observationsMapping, resourcesMapping, propertiesMapping, unitsMapping);

        String simulationEnvironment = commandLine.getOptionValue('e');
        String simulationId = commandLine.getOptionValue('n');

        Model model = publisher.createModel(input, simulationEnvironment, simulationId);

        String format;
        if (commandLine.hasOption('o')) {
            format = commandLine.getOptionValue('f');
        } else {
            format = "TTL";
        }

        if (commandLine.hasOption('o')) {
            try (OutputStreamWriter out = new FileWriter(commandLine.getOptionValue('o'))) {
                model.write(out, format);
            } catch (IOException e) {
                System.err.println("Error while writing output: " + e.getMessage());
                return 1;
            }
        } else {
            model.write(System.out, format);
        }

        return 0;
    }

    private Mapping loadMapping(String path) throws IOException, JAXBException {
        return mappingIO.loadXML(new File(path));
    }

    private List<String> validateCommandLine(CommandLine commandLine) {
        List<String> offenses = new LinkedList<>();

        String requiredParameterMissing = "Missing required parameter";

        if (!commandLine.hasOption('r')) {
            offenses.add(String.format("%s: %s", requiredParameterMissing, "resources mapping"));
        }
        if (!commandLine.hasOption('u')) {
            offenses.add(String.format("%s: %s", requiredParameterMissing, "units mapping"));
        }
        if (!commandLine.hasOption('x')) {
            offenses.add(String.format("%s: %s", requiredParameterMissing, "observation mapping"));
        }
        if (!commandLine.hasOption('p')) {
            offenses.add(String.format("%s: %s", requiredParameterMissing, "properties mapping"));
        }
        if (!commandLine.hasOption('n')) {
            offenses.add(String.format("%s: %s", requiredParameterMissing, "simulation id"));
        }
        if (!commandLine.hasOption('i')) {
            offenses.add(String.format("%s: %s", requiredParameterMissing, "input file path"));
        }
        if (!commandLine.hasOption('e')) {
            offenses.add(String.format("%s: %s", requiredParameterMissing, "output environment"));
        }

        return offenses;
    }

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(Option.builder("r")
                .longOpt("resources")
                .desc("path to the resources mapping file")
                .hasArg()
                .argName("resources")
                .numberOfArgs(1).build());

        options.addOption(Option.builder("p")
                .longOpt("properties")
                .desc("path to the properties mapping file")
                .hasArg()
                .argName("properties")
                .numberOfArgs(1).build());

        options.addOption(Option.builder("u")
                .longOpt("units")
                .desc("path to the units mapping file")
                .hasArg()
                .argName("units")
                .numberOfArgs(1).build());

        options.addOption(Option.builder("x")
                .longOpt("observation")
                .desc("path to the observation mapping file")
                .hasArg()
                .argName("observation")
                .numberOfArgs(1).build());

        options.addOption(Option.builder("n")
                .longOpt("simulation-id")
                .desc("simulation Id")
                .hasArg()
                .argName("simulationId")
                .numberOfArgs(1).build());

        options.addOption(Option.builder("f")
                .longOpt("format")
                .desc("TURTLE (ttl) or XML; default=TTL")
                .hasArg()
                .argName("format")
                .numberOfArgs(1).build());

        options.addOption(Option.builder("i")
                .longOpt("input")
                .desc("input ESO file")
                .hasArg()
                .argName("input")
                .numberOfArgs(1).build());

        options.addOption(Option.builder("o")
                .longOpt("output")
                .required(true)
                .desc("output file path; if not given, output is stdout")
                .hasArg()
                .argName("output")
                .numberOfArgs(1).build());

        options.addOption(Option.builder("e")
                .longOpt("environment")
                .required(true)
                .desc("environment name to export")
                .hasArg()
                .argName("environment")
                .numberOfArgs(1).build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .required(false)
                .desc("displays usage information")
                .hasArg(false).build());

        HelpFormatter helpFormatter = new HelpFormatter();

        Publish publish = new Publish(options, helpFormatter);

        int returnCode = publish.execute(args);

        System.exit(returnCode);
    }
}
