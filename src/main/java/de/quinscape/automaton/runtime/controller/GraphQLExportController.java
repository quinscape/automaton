package de.quinscape.automaton.runtime.controller;

import de.quinscape.automaton.runtime.export.ExportResult;
import de.quinscape.automaton.runtime.export.GraphQLQueryContext;
import de.quinscape.automaton.runtime.export.GraphQLExporter;
import de.quinscape.automaton.runtime.util.GraphQLUtil;
import de.quinscape.domainql.DomainQL;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Controller
public class GraphQLExportController
    implements ApplicationContextAware
{
    private final static Logger log = LoggerFactory.getLogger(GraphQLExportController.class);

    public final static String EXPORT_URI = "/graphql-export";

    private final GraphQL graphQL;

    private final DomainQL domainQL;

    private ApplicationContext applicationContext;


    @Autowired
    public GraphQLExportController(
        @Lazy DomainQL domainQL,
        @Lazy GraphQL graphQL
    )
    {
        this.domainQL = domainQL;
        this.graphQL = graphQL;
    }


    @RequestMapping(value = EXPORT_URI, method = RequestMethod.POST)
    public ResponseEntity<?> export(
        @RequestParam("exporter") String exporterName,
        @RequestParam("query") String query,
        @RequestParam("variables") String paramsJson
    )
    {
        final Map<String,Object> variables = JSONUtil.DEFAULT_PARSER.parse(Map.class, paramsJson);

        log.debug("export: exporter={}, variables={}, query={}", exporterName, variables, query);

        final GraphQLExporter exporter = findExporter(exporterName);
        if (exporter == null)
        {
            return new ResponseEntity<>(
                "No exporter named \"" + exporterName + "\" found",
                HttpStatus.NOT_FOUND
            );
        }

        final Map<String, Object> parameterMap = Map.of(
            "query", query,
            "variables", variables
        );

        final ExecutionResult executionResult = GraphQLUtil.executeGraphQLQuery(
            graphQL,
            parameterMap,
            null
        );

        final Map<String, Object> result = executionResult.toSpecification();
        final Object errors = result.get("errors");
        if (errors != null)
        {
            log.error("Errors in GraphQL query: " + errors);

            return new ResponseEntity<>(
                "Errors in GraphQL query",
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        final GraphQLQueryContext ctx = new GraphQLQueryContext(
            domainQL,
            (String) parameterMap.get("query"),
            (Map<String, Object>) parameterMap.get("variables"),
            (Map<String, Object>) result.get("data")
        );

        try
        {
            final ExportResult<?> export = exporter.export(ctx);

            return new ResponseEntity<>(
                export.body(),
                CollectionUtils.toMultiValueMap(
                    Map.of(
                        HttpHeaders.CONTENT_TYPE, Collections.singletonList(export.contentType()),
                        HttpHeaders.CONTENT_DISPOSITION, Collections.singletonList(
                            "attachment; filename=\"" + export.fileName() + "\""))
                ),
                HttpStatus.OK
            );
        }
        catch (Exception e)
        {
            log.error("Error during export with " + exporter, e);
            return new ResponseEntity<>(
                "Error during export",
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

    }


    private GraphQLExporter findExporter(String exporterName)
    {                                                                         
        try
        {
            return applicationContext.getBean(exporterName, GraphQLExporter.class);
        }
        catch (Exception e)
        {
            log.warn("Error finding exporter " + exporterName);
            return null;
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
}
