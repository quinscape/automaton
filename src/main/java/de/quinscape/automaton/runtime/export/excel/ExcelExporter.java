package de.quinscape.automaton.runtime.export.excel;

import de.quinscape.automaton.runtime.export.ExportResult;
import de.quinscape.automaton.runtime.export.GraphQLExporter;
import de.quinscape.automaton.runtime.export.GraphQLQueryContext;
import de.quinscape.domainql.DomainQL;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;


/**
 * GraphQLExport implementation for Excel sheets. Can be configured via builder including a strategy of how many
 * queries to export into how many excel sheets.
 *
 * @see #newExporter(DomainQL) 
 */
public class ExcelExporter
    implements GraphQLExporter<byte[]>
{
    private final static Logger log = LoggerFactory.getLogger(ExcelExporter.class);


    public final static String EXCEL_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public static final String TIMESTAMP_MARKER = "$now";

    private final DomainQL domainQL;
    private final String metaHeadingName;

    private final String mediaType;

    private final ExportStrategy exportStrategy;

    ExcelExporter(
        DomainQL domainQL,
        ExportStrategy exportStrategy,
        String metaHeadingName,
        String mediaType
    )
    {
        this.domainQL = domainQL;
        this.exportStrategy = exportStrategy;
        this.metaHeadingName = metaHeadingName;
        this.mediaType = mediaType;
    }

    @Override
    public ExportResult<byte[]> export(GraphQLQueryContext ctx) throws IOException
    {
        try
            (
                HSSFWorkbook workbook = new HSSFWorkbook();
                final ByteArrayOutputStream bos = new ByteArrayOutputStream()
            )
        {

            // export impl in here
            ExcelExporterContext exporterContext = new ExcelExporterContext(ctx, workbook, metaHeadingName);

            final String fileName = exportStrategy.generateExport(exporterContext, ctx);

            workbook.write(bos);

            return new ExportResult<>(
                mediaType,
                bos.toByteArray(),
                replaceNow(fileName)
            );
        }
    }


    /**
     * Replaces "$now" in the given name with the current ISO-8601 timestamp.
     *
     * @param name  file name
     *
     * @return file name enriched with current timestamp
     */
    private String replaceNow(String name)
    {
        int pos = name.indexOf(TIMESTAMP_MARKER);

        boolean replace = false;
        if (pos == 0)
        {
            replace = true;
        }
        else
        {
            int slashes = 0;
            while(pos - 1 >= 0 && name.charAt(pos - 1) == '\\')
            {
                slashes++;
                pos--;
            }

            replace = (slashes & 1) == 0;
        }
        if (replace)
        {
            return name.substring(0,pos) + Instant.now().toString() + name.substring(pos + TIMESTAMP_MARKER.length());
        }
        return name;
    }

    /**
     * Expects a single result that is converted into an Excel sheet name with a configured name, exporting the Excel
     * file under a configured name.
     */
    public static class OneQueryStrategy
        implements ExportStrategy
    {
        public final static OneQueryStrategy DEFAULT = new OneQueryStrategy(
            "Automaton Export",
            "automaton-$now.xlsx");

        private final String sheetName;
        private final String fileName;


        public OneQueryStrategy(String sheetName, String fileName)
        {
            this.sheetName = sheetName;
            this.fileName = fileName;
        }


        @Override
        public String generateExport(ExcelExporterContext ctx, GraphQLQueryContext queryContext)
        {
            final GraphQLQueryContext.MethodResult mr = queryContext.getOnlyResult();
            ctx.addSheet(sheetName, mr);
            return fileName;
        }
    }

    public static ExcelExporterBuilder newExporter(DomainQL domainQL)
    {
        return new ExcelExporterBuilder(domainQL);
    }

}
