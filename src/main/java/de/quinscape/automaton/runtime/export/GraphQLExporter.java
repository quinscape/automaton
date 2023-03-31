package de.quinscape.automaton.runtime.export;

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
}
