package de.quinscape.automaton.runtime.export.excel;

import de.quinscape.automaton.runtime.export.GraphQLExportException;
import de.quinscape.automaton.runtime.export.GraphQLExporter;
import de.quinscape.automaton.runtime.export.GraphQLQueryContext;
import de.quinscape.automaton.runtime.util.SchemaReference;
import de.quinscape.domainql.DomainQL;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeUtil;
import jakarta.validation.constraints.NotNull;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSON;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generation context for the {@link GraphQLExporter}. Provided to the {@link ExportStrategy} to control operations.
 */
public final class ExcelExporterContext
{
    private final static Logger log = LoggerFactory.getLogger(ExcelExporterContext.class);

    private final GraphQLQueryContext queryContext;

    private final HSSFWorkbook workbook;

    private final String metaHeadingName;

    private final Map<String, Integer> usedSheetNames = new HashMap<>();


    public ExcelExporterContext(
        GraphQLQueryContext queryContext, HSSFWorkbook workbook,
        String metaHeadingName
    )
    {
        this.queryContext = queryContext;
        this.workbook = workbook;
        this.metaHeadingName = metaHeadingName;
    }


    /**
     * Adds a new Excel sheet with the data from the given MethodResult
     *
     * @param sheetName     Name of the new sheet
     * @param mr            method result
     */
    public void addSheet(String sheetName, GraphQLQueryContext.MethodResult mr)
    {
        addSheet(sheetName, mr, null);
    }
    /**
     * Adds a new Excel sheet with the data from the given MethodResult
     *
     * @param sheetName         Name of the new sheet
     * @param mr                method result
     * @param fieldPredicate    predicate used to filter the schema references of the column states
     */
    public void addSheet(String sheetName, GraphQLQueryContext.MethodResult mr, Predicate<SchemaReference> fieldPredicate)
    {
        log.debug(
            "Exporting: {} = {}",
            mr.methodReference(),
            JSON.formatJSON(JSONUtil.DEFAULT_GENERATOR.forValue(mr.methodResult()))
        );

        final String uniqueSheetName = makeUnique(sheetName);

        final Object value = mr.methodResult();
        final Map<String, Object> iQuery = validateIQuery(value);

        String rootType = (String) iQuery.get("type");
        List<Map<String, Object>> columnStates = (List<Map<String, Object>>) iQuery.get("columnStates");
        List<Map<String, Object>> rows = (List<Map<String, Object>>) iQuery.get("rows");

        final DomainQL domainQL = queryContext.domainQL();
        final SchemaReference root = SchemaReference.newRef(
            domainQL,
            rootType
        );

        final HSSFSheet sheet = workbook.createSheet(uniqueSheetName);
        final HSSFRow headingsRow = sheet.createRow(0);

        List<Map<String, Object>> enabledColumns = columnStates.stream()
            .filter(
                state -> (boolean) state.get("enabled") &&
                    (fieldPredicate == null || fieldPredicate.test(
                        root.getField((String) state.get("name"))
                    ))
            )
            .collect(Collectors.toList());

        for (int i = 0; i < enabledColumns.size(); i++)
        {
            Map<String, Object> columnState = enabledColumns.get(i);
            final HSSFCell cell = headingsRow.createCell(i);
            final String columnName = (String) columnState.get("name");
            final SchemaReference column = root.getField(columnName);

            String heading = column.getMeta(metaHeadingName);
            cell.setCellValue(heading != null ? heading : columnName);
        }

        for (int i = 0; i < rows.size(); i++)
        {
            Map<String, Object> row = rows.get(i);
            final HSSFRow dataRow = sheet.createRow(i + 1);
            for (int j = 0; j < enabledColumns.size(); j++)
            {
                Map<String, Object> columnState = enabledColumns.get(j);
                final HSSFCell cell = dataRow.createCell(j);

                final String columnName = (String) columnState.get("name");
                final SchemaReference column = SchemaReference.newRef(
                    domainQL,
                    rootType,
                    columnName
                );

                if (!column.isScalar())
                {
                    throw new ExcelExportException(rootType + "." + columnName + " is no scalar");
                }

                final GraphQLOutputType origType = column.getOriginalType();
                final GraphQLScalarType scalarType = (GraphQLScalarType) GraphQLTypeUtil.unwrapAll(origType);

                final Object fieldValue = column.get(row);
                final Object converted = convertScalar(scalarType, fieldValue);
                setCellValue(cell, scalarType, converted);
            }
        }

    }


    /**
     * Returns the POI workbook.
     *
     * @return workbook
     */
    public HSSFWorkbook getWorkbook()
    {
        return workbook;
    }


    public String getMetaHeadingName()
    {
        return metaHeadingName;
    }


    /**
     * Sets the given POI cell with the given value
     *
     * @param cell          POI cell
     * @param scalarType    GraphQL scalar type
     * @param value         value
     */
    private static void setCellValue(HSSFCell cell, GraphQLScalarType scalarType, Object value)
    {
        if (value instanceof List)
        {
            StringBuilder buf = new StringBuilder();
            List<?> list = (List<?>) value;
            for (int i = 0; i < list.size(); i++)
            {
                if (i != 0)
                {
                    buf.append(", ");
                }
                Object o = list.get(i);
                buf.append(o);
            }

            cell.setCellValue(buf.toString());
        }
        else if (value instanceof String)
        {
            cell.setCellValue((String) value);
        }
        else if (value instanceof Number)
        {
            cell.setCellValue(((Number) value).doubleValue());
        }
        else if (scalarType.getName().equals("Date"))
        {
            cell.setCellValue((LocalDate) value);
        }
        else if (scalarType.getName().equals("TimeStamp"))
        {
            cell.setCellValue((LocalDateTime) value);
        }
        else
        {
            cell.setCellValue(value == null ? "" : value.toString());
        }
    }


    /**
     * Returns a unique sheet name based on the given name.
     *
     * @param name  base name
     *
     * @return unique name
     */
    private String makeUnique(String name)
    {
        final int num = usedSheetNames.getOrDefault(name, 0) + 1;
        usedSheetNames.put(name, num);
        if (num == 1)
        {
            return name;
        }
        return name + " (" + num + ")";
    }


    /**
     * Validates the GraphQL data to be a plausible InteractiveQuery.
     *
     * @param value JSON graph
     *
     * @return a roughly validated iQuery document
     */
    private Map<String,Object> validateIQuery(Object value)
    {
        final Set<Member> members = Set.of(
            new Member("type", String.class),
            new Member("columnStates", List.class),
            new Member("rows", List.class),
            new Member("rowCount", Integer.class)
        );

        if (!(value instanceof  Map))
        {
            throw new GraphQLExportException("IQuery value is not a map");
        }

        Map<String, Object> iQuery = (Map<String, Object>) value;
        for (Member member : members)
        {
            final String name = member.name();
            final Class<?> cls = member.cls;

            final Object v = iQuery.get(name);
            if (!cls.isInstance(v))
            {
                throw new IllegalStateException("Invalid iQuery document. Key \"" + name + "\" is not of type " + cls + ": " + iQuery);
            }
        }

        return iQuery;
    }


    /**
     * Converts the given scalar for Excel
     *
     * @param scalarType    GraphQL scalar type
     * @param fieldValue    value
     *
     * @return Excel value
     * */
    @NotNull
    private static Object convertScalar(GraphQLScalarType scalarType, Object fieldValue)
    {
        final Object converted;
        if (fieldValue instanceof List)
        {
            List<Object> list = (List<Object>) fieldValue;
            final ArrayList<Object> convertedList = new ArrayList<>(list.size());

            for (Object elem : list)
            {
                convertedList.add( elem != null ? scalarType.getCoercing().parseValue(
                    elem) : null);
            }
            converted = convertedList;
        }
        else
        {
            converted = fieldValue != null ? scalarType.getCoercing().parseValue(
                fieldValue) : null;
        }
        return converted;
    }


    /**
     * JSON graph object member
     * 
     * @param name      member name
     * @param cls       expected class
     */
    private record Member(String name, Class<?> cls ) {
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "workbook = " + workbook
            + ", metaHeadingName = '" + metaHeadingName + '\''
            ;
    }
}
