package de.quinscape.automaton.runtime.export.json;

import de.quinscape.automaton.runtime.export.ExportResult;
import de.quinscape.automaton.runtime.export.GraphQLExporter;
import de.quinscape.automaton.runtime.export.GraphQLQueryContext;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.springframework.http.MediaType;

import java.time.Instant;

/**
 * Exports the given GraphQL result as JSON data.
 */
public class JSONExporter
    implements GraphQLExporter<String>
{
    @Override
    public ExportResult<String> export(GraphQLQueryContext ctx)
    {
        final String json = JSONUtil.DEFAULT_GENERATOR.forValue(ctx.data());
        final String fileName = "iquery-" + Instant.now().toString() + ".json";

        return new ExportResult<>(
            MediaType.APPLICATION_JSON_VALUE,
            JSONUtil.formatJSON(json),
            fileName
        );
    }
}
