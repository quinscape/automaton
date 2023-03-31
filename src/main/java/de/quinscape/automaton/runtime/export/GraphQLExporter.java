package de.quinscape.automaton.runtime.export;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Implemented by classes that export Interactive Query data in other data format.
 *
 * @param <T>   Java content type
 */
public interface GraphQLExporter<T>
{
    /**
     * Runs an export operation for the given content type and GraphQL query result
     *
     * @param ctx   Exporter context
     *
     * @return export result
     */
    ExportResult<T> export(GraphQLQueryContext ctx) throws Exception;

    /**
     * Constant of magic constant that gets replaced with the current ISO-8601 date in export names
     */
    String DOLLAR_NOW = "$now";

    /**
     * Replaces "$now" with the current ISO-8601 timestamp.
     *
     * @param name  file name
     *
     * @return file name enriched with current timestamp
     */
    static String replaceNow(String name)
    {
        int pos = name.indexOf(DOLLAR_NOW);

        boolean replace = false;
        int slashes = 0;
        int orig = pos;
        if (pos == 0)
        {
            replace = true;
        }
        else
        {
            while(pos - 1 >= 0 && name.charAt(pos - 1) == '\\')
            {
                slashes++;
                pos--;
            }

            replace = (slashes & 1) == 0;
        }
        if (replace)
        {
            return name.substring(0,orig) + Instant.now().truncatedTo(ChronoUnit.SECONDS) + name.substring(orig + DOLLAR_NOW.length());
        }

        // We have a \\$now and need to remove the \\
        if (slashes > 0)
        {
            return name.substring(0,pos) + name.substring(pos + 1);
        }

        return name;
    }
}
