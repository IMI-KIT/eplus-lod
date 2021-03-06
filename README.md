# EnergyPlus LOD

Exposes EnergyPlus simulation output files as linked open data.

## Concept

Conceptually, the exporter draws upon the SSN Ontology. The best way to get
started is to familiarize yourself with the ontology by reading the
[original paper](http://dx.doi.org/10.1016/j.websem.2012.05.003).

The central idea of the SSN ontology is manifested in the Observation Pattern.
It names four distinct entities: the _feature of interest_, or the subject of
observation, the _property_, or the aspect of the feature that is being
observed, the _sensor_ that has performed the measurement, and the _observation
result_.

This tool assumes that the four entities are indicated in the data
dictionary section of the simulation output file. For example, given the line
```
21,11,Electricity:Building [J] !RunPeriod [Value,Min,Month,Day,Hour,Minute,Max,Month,Day,Hour,Minute]
```
we can assign:

 * `Electricity` to the property,
 * `Building` to the feature of interest,
 * `21` to the sensor (technically, it is the simulation variable's report code),
 * The combination of the above to the observation result.

Additionally, the line indicates the unit of measurement, denoted in this case
by J, which is the symbol for the joule.

If the exporter can distinguish those five entities in the data dictionary, it
will produce an Observation and a corresponding Observation Result from the
combination. The disambiguation is guided by four
[mapping files](https://github.com/attadanta/rdf-mapper) that serve
as a configuration. They are described below.

### Resources

The `Resources.xml` file describes the mapping of the features of interest.
The entity names should correspond to the strings found in the data dictionary.
Example:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<mapping xmlns="http://imi.kit.edu/rdfMapping">
    <namespaces>
        <!-- snip -->
    </namespaces>

    <entities>
        <entity>
            <uri>:${simulationId}/Facility</uri>
            <name>Facility</name>
            <type>eplus:SimulationResource</type>
            <type>ssn:FeatureOfInterest</type>
        </entity>
    </entities>
</mapping>
```

This will match `Facility` in the data dictionary and, if the other components
are also present, construct an Observation linking to the Feature of Interest
with the indicated `uri` pattern.

Note that you can completely customize the URL mapping and the types
assignment. Should you decide not to align with the SSN ontology, you are free to omit
the Feature Of Interest type assignment. You could also assign object and data
properties to the Facility entity. Keep in mind, however, that the entity will
be mapped against the data dictionary item, which may not provide enough context
to define the property values sufficiently.

The `simulationId` fragment serves to distinguish the originating simulation
run and is optional. This named variable is injected externally via a command-
line parameter as we shall see below.

### Properties

Observation properties are configured in the `Properties.xml` file. An example
is given below.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<mapping xmlns="http://imi.kit.edu/rdfMapping">
    <namespaces>
        <!-- snip -->
    </namespaces>

    <entities>
        <entity>
            <uri>:${simulationId}/${resource}/electricity</uri>
            <name>Electricity</name>
            <type>ssn:Property</type>
        </entity>
        <entity>
            <uri>:${simulationId}/${resource}/co2</uri>
            <name>CO2</name>
            <type>ssn:Property</type>
        </entity>
    </entities>
</mapping>
```

Again, the `name` tag is matched against the data dictionary item and hence
must be identical with the data dictionary contents.

To build URLs for the properties, we can use the `resource` named variable
to indicate a logical link with the resource entity. To make the relationship
explicit, you could also add a [ssn:isPropertyOf](https://www.w3.org/2005/Incubator/ssn/ssnx/ssn#isPropertyOf)
object property assignment pointing to the `resourceURL`.

### Units

`Units.xml` configure units mappings. The example below demonstrates aligning
against the [OM](http://www.wurvoc.org/vocabularies/om-1.8/) ontology.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<mapping xmlns="http://imi.kit.edu/rdfMapping">
    <namespaces>
        <!-- snip -->
    </namespaces>
    <entities>
        <entity>
            <uri>om:kilogram</uri>
            <name>kg</name>
        </entity>
        <entity>
            <uri>om:joule</uri>
            <name>J</name>
        </entity>
        <entity>
            <uri>om:cubic_metre</uri>
            <name>m3</name>
        </entity>
    </entities>
</mapping>
```

Here, we map directly to the concepts of the OM ontology.

### Observation

Lastly, we interlink the concepts described thus far by implementing the
Observation Pattern. The configuration is given by `Observation.xml`,
which should define the following entity mappings.

#### Sensor

The `Sensor` mapping distinguishes the Sensor entity of the pattern. Below, we
assume that this is represented by the report code of the simulation output
variable:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<mapping xmlns="http://imi.kit.edu/rdfMapping">
    <!-- snip -->
    <entity>
        <uri>:${simulationId}/sensors/${variableId}</uri>
        <name>Sensor</name>
    </entity>
    <!-- snip -->
</mapping>
```

#### Observation Result

The `ObservationResult` entity collects all references to the obtained
simulation values. Below, we use a combination of the `variableId` and
`property` variables to construct its URL.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<mapping xmlns="http://imi.kit.edu/rdfMapping">
    <!-- snip -->
    <entity>
        <uri>:${simulationId}/observation/${variableId}/${property}/result</uri>
        <name>ObservationResult</name>
        <type>eplus:OutputDataset</type>
        <type>ssn:ObservationResult</type>
    </entity>
    <!-- snip -->
</mapping>
```

#### Observation

The `Observation` mapping configures the central entity in the Observation
pattern. By now, it should be obvious how to construct it keeping in mind
that the mapper remembers the URLs of the entities thus far.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<mapping xmlns="http://imi.kit.edu/rdfMapping">
    <!-- snip -->
    <entity>
        <uri>:${simulationId}/observations/${variableId}/${property}</uri>
        <name>Observation</name>
        <type>ssn:Observation</type>
        <properties>
            <objectProperty>
                <uri>ssn:featureOfInterest</uri>
                <object>${resourceURL}</object>
            </objectProperty>
            <objectProperty>
                <uri>ssn:observedProperty</uri>
                <object>${propertyURL}</object>
            </objectProperty>
            <objectProperty>
                <uri>ssn:observationResult</uri>
                <object>${observationResultURL}</object>
            </objectProperty>
            <objectProperty>
                <uri>ssn:observedBy</uri>
                <object>${sensorURL}</object>
            </objectProperty>
        </properties>
    </entity>
    <!-- snip -->
</mapping>
```

#### Observation Value

Finally, we specify how to map observation values. Conveniently, every named
variable that was mapped during the construction of the Observation entity is
available by the time the values are exported. Additionally, the report
line can also be accessed by its numbered components: `#{0}` maps to the
report code and `#{1}` to the numerical value.

In the example below, we demonstrate how the detected unit of measurement is
integrated within the frame of the OM ontology.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<mapping xmlns="http://imi.kit.edu/rdfMapping">
    <!-- snip -->
    <entity>
        <uri></uri>
        <name>ObservationValue</name>
        <type>qb:Observation</type>
        <type>ssn:ObservationValue</type>
        <type>eplus:SimulationValue</type>
        <type>om:Measure</type>
        <properties>
            <objectProperty>
                <uri>qb:dataSet</uri>
                <object>${observationResultURL}</object>
            </objectProperty>
            <dataProperty>
                <uri>om:numerical_value</uri>
                <value>#{1}</value>
            </dataProperty>
            <objectProperty>
                <uri>om:unit_of_measure_or_measurement_scale</uri>
                <object>${unitURL}</object>
            </objectProperty>
        </properties>
    </entity>
    <!-- snip -->
</mapping>
```

Note that in the above example we have omitted specifying a URL for the value
entity. In that case, the mapper produces an anonymous subject. Of course,
you can opt to assign a URL from the combination of the properties in the
value context.

## Usage

### Prerequisites

The library builds with maven and requires Java 8. Additionally, until it is
released proper, you need to download and install its dependencies
[eplus-io](https://github.com/attadanta/energyplus-io) and
[rdf-mapper](https://github.com/attadanta/rdf-mapper) manually, in that order.

### Exporting EnergyPlus Data

An executable jar can be produced via `mvn package`. If that succeeds,
everything is set.

You can find a full set of mapping files and a sample output file in the
`/src/test/resources` folder. You may want to review them before proceeding.

A RDF file can be produced directly by the jar file like this:
```sh
java -jar eplus-lod-0.0.0.jar \
    --properties Properties.xml \
    --resources Resources.xml \
    --units Units.xml \
    --observation Observation.xml \
    --input eplusout.eso \
    --output export.ttl \
    --format TTL \
    --simulation-id 42 \
    --environment RESIDENTIAL
```

This will assign the value `42` to the `simulationId` variable and export
the data in the `RESIDENTIAL` output environment in `eplusout.eso` into the
file `export.ttl` using the TURTLE format.

## Command-Line Reference

```
 -e,--environment <environment>       environment name to export
 -f,--format <format>                 TURTLE (ttl) or XML; default=TTL
 -h,--help                            displays usage information
 -i,--input <input>                   input ESO file
 -n,--simulation-id <simulation id>   simulation Id
 -o,--output <output>                 output file path; if not given,
                                      output is stdout
 -p,--properties <properties>         path to the properties mapping file
 -r,--resources <resources>           path to the resources mapping file
 -u,--units <units>                   path to the units mapping file
 -x,--observation <observation>       path to the observation mapping file
```

## Variables Reference

These are the named variables currently emitted by the library:

 * simulationId
 * property
 * resource
 * unit
 * variableId
 * propertyURL
 * resourceURL
 * unitURL
 * sensorURL
 * observationURL
 * observationResultURL

## Acknowledgment

This work originated in the [DAREED](http://dareed.eu) project. DAREED has
received funding from the the Seventh framework programme of the European
union (EU) under grant agreement 609082.