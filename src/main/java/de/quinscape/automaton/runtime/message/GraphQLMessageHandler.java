package de.quinscape.automaton.runtime.message;

import de.quinscape.automaton.model.message.IncomingMessage;
import de.quinscape.automaton.runtime.ws.AutomatonClientConnection;
import de.quinscape.domainql.DomainQL;
import graphql.ExecutionInput;
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

        String query = (String) mapIn.get("query");
        Map<String, Object> variables = (Map<String, Object>) mapIn.get("variables");
        //log.debug("/graphql: query = {}, vars = {}", query, variables);

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            .query(query)
            .variables(variables)
            .context(connection.getConnectionId())
            .build();

        Map<String, Object> result = graphQL.execute(executionInput).toSpecification();

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
