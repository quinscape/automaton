package de.quinscape.automaton.runtime.export.excel;

import de.quinscape.automaton.runtime.export.GraphQLQueryContext;

/**
 * Strategy interface implemented by lambda expressions controlling the export operation of the {@link ExcelExporter}
 */
public interface ExportStrategy
{
    /**
     * Generates the Excel export by getting data from the given query context and then calling methods on the exporter
     * context.
     *
     * @param ctx               exporter context
     * @param queryContext      query context
     *
     * @return fileName to use for the Excel file (can contain "$now" placeholder)
     */
    String generateExport(ExcelExporterContext ctx, GraphQLQueryContext queryContext);
}
