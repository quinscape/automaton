package de.quinscape.automaton.runtime.controller;

import de.quinscape.automaton.runtime.util.GraphQLUtil;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

@Controller
public class GraphQLController
{
    private final static Logger log = LoggerFactory.getLogger(GraphQLController.class);


    private final GraphQL graphQL;

    /**
     * URI for the normal application data usage. Is always under general Spring security protection which includes
     * CSRF protection.
     */
    public final static String GRAPHQL_URI = "/graphql";

    /**
     * Special development GraphQL end point that is active if 
     */
    public final static String GRAPHQL_DEV_URI = "/graphql-dev";


    @Autowired
    public GraphQLController(
        @Lazy GraphQL graphQL
    )
    {
        this.graphQL = graphQL;
    }


    @RequestMapping(value = GRAPHQL_URI, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> serveGraphQL(
        @RequestBody Map body
        //@RequestParam("cid") String connectionId
    )
    {
        log.debug("Received query: {}", body);

        return executeGraphQLQuery(body, null);
    }


    /**
     * Special development graphql endpoint that is accessible without CSRF protection if the environment property
     * <em>automatontest.graphql.dev</em> is set to <code>true</code>.
     *
     * This dedicated end point allows access to GraphQL queries from the IDE (without session/CSRF). 
     *
     * @param body      request body
     * @return GraphQL response entity
     */
    @Profile("dev")
    @RequestMapping(value = GRAPHQL_DEV_URI, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> serveGraphQLDev(@RequestBody Map body)
    {
        return executeGraphQLQuery(body, null);
    }


    private ResponseEntity<String> executeGraphQLQuery(@RequestBody Map queryMap, Object context)
    {
        ExecutionResult executionResult = GraphQLUtil.executeGraphQLQuery(graphQL, queryMap, context);

        final List<GraphQLError> errors = executionResult.getErrors();
        if (errors.size() > 0)
        {
            log.warn("Errors in graphql query: " + GraphQLUtil.formatErrors(errors));
        }

        // result may contain data and/or errors
        Object result = executionResult.toSpecification();
        return new ResponseEntity<String>(
            JSONUtil.DEFAULT_GENERATOR.forValue(
                result
            ),
            errors.size() == 0 ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        );
    }
}
