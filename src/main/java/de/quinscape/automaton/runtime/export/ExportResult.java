package de.quinscape.automaton.runtime.export;

/**
 * Export result from a {@link GraphQLExporter}.
 *
 * @param contentType   HTTP content type
 * @param body          export data
 * @param fileName      file name to suggest to the client
 * @param <T>           export data type
 */
public record ExportResult<T>(String contentType, T body, String fileName)
{

}
