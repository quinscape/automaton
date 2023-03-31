package de.quinscape.automaton.runtime.export.excel;

import de.quinscape.domainql.DomainQL;

/**
 * Builder for the {@link ExcelExporter}.
 */
public class ExcelExporterBuilder
{
    private final DomainQL domainQL;

    private String metaHeadingName = "heading";

    private String mediaType = ExcelExporter.EXCEL_MEDIA_TYPE;

    private ExportStrategy exportStrategy = ExcelExporter.OneQueryStrategy.DEFAULT;

    ExcelExporterBuilder(DomainQL domainQL)
    {

        this.domainQL = domainQL;
    }


    public DomainQL getDomainQL()
    {
        return domainQL;
    }




    /**
     * Returns the configured meta name
     * 
     * @return meta named used for the heading
     */
    public String getMetaHeadingName()
    {
        return metaHeadingName;
    }


    /**
     * Configures the field meta that is used as heading instead of the field path if present. Default is "heading"
     * @param metaHeadingName meta name
     *
     * @return this builder
     */
    public ExcelExporterBuilder withMetaHeadingName(String metaHeadingName)
    {
        this.metaHeadingName = metaHeadingName;
        return this;
    }


    /**
     * Returns the configured media type to use.
     * Default is {@link ExcelExporter#EXCEL_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"}
     * @return media type
     */
    public String getMediaType()
    {
        return mediaType;
    }


    /**
     * Configures the media type to support and export.
     * Default is {@link ExcelExporter#EXCEL_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"}
     *
     * @param mediaType media type
     *
     * @return this builder
     */
    public ExcelExporterBuilder withMediaType(String mediaType)
    {
        this.mediaType = mediaType;
        return this;
    }


    public ExportStrategy getExportStrategy()
    {
        return exportStrategy;
    }


    /**
     * Defines an alternative export strategy. The default one {@link ExcelExporter.OneQueryStrategy}
     * @param exportStrategy
     * @return
     */
    public ExcelExporterBuilder withExportStrategy(ExportStrategy exportStrategy)
    {
        this.exportStrategy = exportStrategy;
        return this;
    }


    /**
     * Builds the configured Excel exporter.
     *
     * @return Excel exporter
     */
    public ExcelExporter build()
    {
        return new ExcelExporter(domainQL, exportStrategy, metaHeadingName, mediaType);
    }
}
