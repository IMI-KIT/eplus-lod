package eu.dareed.eplus.lod;

import eu.dareed.eplus.model.eso.ESO;
import eu.dareed.eplus.parsers.eso.ESOParser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.MatchResult;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class DictionaryLineParserTest {
    private static DictionaryLineParser parser;

    @BeforeClass
    public static void setup() throws IOException {
        ESO eso;
        try (InputStream stream = DictionaryLineParserTest.class.getResourceAsStream("/fixtures/outputs/eplusout.eso")) {
            eso = new ESOParser().parseFile(stream);
        }
        eso.getDataDictionary();

        DictionaryLineParserTest.parser = new DictionaryLineParser();
    }

    @Test
    public void testParseMeterOutput0() {
        OutputMetadata output = parser.parseOutput("CH4:Facility [kg] !RunPeriod [Value,Min,Month,Day,Hour,Minute,Max,Month,Day,Hour,Minute]");

        Assert.assertEquals("CH4:Facility", output.name);
        Assert.assertTrue(output.hasCompoundName());
        Assert.assertEquals("kg", output.unit);
        Assert.assertEquals(ReportFrequency.RunPeriod, output.reportFrequency);
        Assert.assertArrayEquals(new String[] {"Value","Min","Month","Day","Hour","Minute","Max","Month","Day","Hour","Minute"}, output.schema.toArray());
    }

    @Test
    public void testParseMeterOutput1() {
        OutputMetadata output = parser.parseOutput("Zone Thermal Comfort ASHRAE 55 Simple Model Summer or Winter Clothes Not Comfortable Time [hr] !RunPeriod [Value,Min,Month,Day,Hour,Minute,Max,Month,Day,Hour,Minute]");

        Assert.assertEquals("Zone Thermal Comfort ASHRAE 55 Simple Model Summer or Winter Clothes Not Comfortable Time", output.name);
        Assert.assertFalse(output.hasCompoundName());
        Assert.assertEquals("hr", output.unit);
        Assert.assertEquals(ReportFrequency.RunPeriod, output.reportFrequency);
        Assert.assertArrayEquals(new String[] {"Value","Min","Month","Day","Hour","Minute","Max","Month","Day","Hour","Minute"}, output.schema.toArray());
    }

    @Test
    public void testParseMeterOutput2() {
        OutputMetadata output = parser.parseOutput("Electricity:Plant [J] !Hourly");

        Assert.assertEquals("Electricity:Plant", output.name);
        Assert.assertEquals("J", output.unit);
        Assert.assertEquals(ReportFrequency.Hourly, output.reportFrequency);
        Assert.assertTrue(output.hasCompoundName());
        Assert.assertTrue(output.schema.isEmpty());
    }

    @Test
    public void testMatchMeter0() {
        MatchResult matcher = parser.matchOutput("CH4:Facility [kg] !RunPeriod [Value,Min,Month,Day,Hour,Minute,Max,Month,Day,Hour,Minute]");

        Assert.assertEquals("CH4:Facility", matcher.group(1));
        Assert.assertEquals("kg", matcher.group(2));
        Assert.assertEquals("RunPeriod", matcher.group(3));
        Assert.assertEquals("Value,Min,Month,Day,Hour,Minute,Max,Month,Day,Hour,Minute", matcher.group(5));
    }

    @Test
    public void testMatchMeter1() {
        MatchResult output = parser.matchOutput("Zone Thermal Comfort ASHRAE 55 Simple Model Summer or Winter Clothes Not Comfortable Time [hr] !RunPeriod [Value,Min,Month,Day,Hour,Minute,Max,Month,Day,Hour,Minute]");
        Assert.assertNotNull(output);
    }

    @Test
    public void testMatchMeter2() {
        MatchResult output = parser.matchOutput("Electricity:Plant [J] !Hourly");
        Assert.assertNotNull(output);
    }
}