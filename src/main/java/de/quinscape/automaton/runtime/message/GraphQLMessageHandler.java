package de.quinscape.automaton.runtime.message;

import de.quinscape.automaton.model.message.IncomingMessage;
import de.quinscape.automaton.runtime.util.GraphQLUtil;
import de.quinscape.automaton.runtime.ws.AutomatonClientConnection;
import de.quinscape.domainql.DomainQL;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;

import java.util.List;
import java.util.Map;

/**
 * Performs GraphQL requests over websocket.
 */
public class GraphQLMessageHandler
    implements IncomingMessageHandler
{
    public static final String TYPE = "GRAPHQL";

    private final DomainQL domainQL;

    private final GraphQL graphQL;


    public GraphQLMessageHandler(DomainQL domainQL)
    {
        this.domainQL = domainQL;
        this.graphQL = GraphQL.newGraphQL(
            domainQL.getGraphQLSchema()
        ).build();
    }


    @Override
    public String getMessageType()
    {
        return TYPE;
    }


    @Override
    public void handle(IncomingMessage msg, AutomatonClientConnection connection)
    {

        Map<String,Object> mapIn = (Map<String, Object>) msg.getPayload();

        //log.debug("/graphql: query = {}, vars = {}", query, variables);

        final ExecutionResult executionResult = GraphQLUtil.executeGraphQLQuery(
            graphQL,
            mapIn,
            connection.getConnectionId()
        );

        Map<String, Object> result = executionResult.toSpecification();

        final List errors = (List) result.get("errors");
        if (errors != null && errors.size() > 0)
        {
            connection.respondWithError(msg.getMessageId(), errors);
        }
        else
        {
            connection.respond(msg.getMessageId(), result);
        }
    }
}
