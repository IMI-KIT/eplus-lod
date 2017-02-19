package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.eso.ESO;
import eu.dareed.eplus.parsers.eso.ESOParser;
import eu.dareed.rdfmapper.MappingIO;
import eu.dareed.rdfmapper.xml.nodes.Mapping;
import org.apache.commons.cli.*;
import org.apache.jena.rdf.model.Model;

import javax.xml.bind.JAXBException;
import java.io.*;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class Publish {
    private final Options options;
    private final HelpFormatter helpFormatter;
    private final MappingIO mappingIO;

    Publish(Options options, HelpFormatter helpFormatter) {
        this.options = options;
        this.helpFormatter = helpFormatter;
        this.mappingIO = new MappingIO();
    }

    int execute(String[] args) {
        CommandLineParser parser = new DefaultParser();

        CommandLine commandLine;

        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException exp) {
            System.err.println(exp.getMessage());
            return 1;
        }

        if (commandLine.hasOption('h')) {
            helpFormatter.printHelp(
                    "publish",
                    "Exposes EnergyPlus simulation output files as linked open data\n\n",
                    options,
                    "Please, report issues at https://github.com/IMI-KIT/eplus-lod/issues",
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

        try (OutputStreamWriter out = new FileWriter(commandLine.getOptionValue('o'))) {
            model.write(out, commandLine.getOptionValue('f'));
        } catch (IOException e) {
            System.err.println("Error while writing output: " + e.getMessage());
            return 1;
        }

        return 0;
    }

    Mapping loadMapping(String path) throws IOException, JAXBException {
        return mappingIO.loadXML(new File(path));
    }

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(Option.builder("r")
                .longOpt("resources")
                .required(true)
                .desc("path to the resources mapping file")
                .hasArg()
                .numberOfArgs(1).build());

        options.addOption(Option.builder("p")
                .longOpt("properties")
                .required(true)
                .desc("path to the properties mapping file")
                .hasArg()
                .numberOfArgs(1).build());

        options.addOption(Option.builder("u")
                .longOpt("units")
                .required(true)
                .desc("path to the units mapping file")
                .hasArg()
                .numberOfArgs(1).build());

        options.addOption(Option.builder("x")
                .longOpt("observation")
                .required(true)
                .desc("path to the observation mapping file")
                .hasArg()
                .numberOfArgs(1).build());

        options.addOption(Option.builder("n")
                .longOpt("simulation-id")
                .required(true)
                .desc("simulation Id")
                .hasArg()
                .numberOfArgs(1).build());

        options.addOption(Option.builder("f")
                .longOpt("format")
                .required(true)
                .desc("TURTLE (ttl) or XML")
                .hasArg()
                .numberOfArgs(1).build());

        options.addOption(Option.builder("i")
                .longOpt("input")
                .required(true)
                .desc("input ESO file")
                .hasArg()
                .numberOfArgs(1).build());

        options.addOption(Option.builder("o")
                .longOpt("output")
                .required(true)
                .desc("output file path")
                .hasArg()
                .numberOfArgs(1).build());

        options.addOption(Option.builder("e")
                .longOpt("environment")
                .required(true)
                .desc("environment name to export")
                .hasArg()
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
