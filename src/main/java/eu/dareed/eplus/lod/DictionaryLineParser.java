package eu.dareed.eplus.lod;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
class DictionaryLineParser implements Predicate<String>, Function<String, OutputMetadata> {

    private static final Pattern outputPattern = Pattern.compile("(.+) \\[(\\w+)] !(\\w+)( \\[(.+)])?");

    OutputMetadata parseOutput(String output) {
        MatchResult matcher = matchOutput(output);

        String name = matcher.group(1);
        String unit = matcher.group(2);
        String reportFrequencyLiteral = matcher.group(3);

        String schemaLiteral = matcher.group(5);
        List<String> schema = schemaLiteral != null ? Arrays.asList(schemaLiteral.split(",")) : null;

        ReportFrequency reportFrequency = ReportFrequency.valueOf(reportFrequencyLiteral);

        return new OutputMetadata(name, unit, reportFrequency, schema);
    }

    MatchResult matchOutput(String output) {
        Matcher matcher = outputPattern.matcher(output);

        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }

        return matcher;
    }

    @Override
    public boolean test(String output) {
        Matcher matcher = outputPattern.matcher(output);
        if (!matcher.matches()) {
            return false;
        }

        try {
            ReportFrequency.valueOf(matcher.group(3));
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }

    @Override
    public OutputMetadata apply(String output) {
        return parseOutput(output);
    }
}
