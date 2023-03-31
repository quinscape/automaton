package de.quinscape.automaton.runtime.export;

import de.quinscape.automaton.runtime.util.SchemaReference;
import de.quinscape.domainql.DomainQL;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates the inputs and result of a single server-side GraphQL document evaluation potentially containing multiple
 * methods being called.
 */
public record GraphQLQueryContext(
    /**
     * DomainQL instance
     */
    DomainQL domainQL,
    /**
     * Query string
     */
    String query,
    /**
     * Query variables
     */
    Map<String, Object> variables,
    /**
     * Result data
     */
    Map<String, Object> data

    /**
     * Content type used for GraphQL export
     */
)
{

    /**
     * Assumes there is only one method result in the GraphQL data and returns that method result.
     *
     * @return the one and only method result
     * @throws GraphQLExportException if there is more than one result or no result
     */
    public MethodResult getOnlyResult()
    {
        if (data.size() != 1)
        {
            throw new GraphQLExportException("Excepted exactly one result in GraphQL Data, have " + data.keySet() );
        }

        String name = data.keySet().iterator().next();
        return getMethodResult(name);
    }

    public Set<String> getMethodNames()
    {
        return Collections.unmodifiableSet(data.keySet());
    }

    /**
     * Returns the method result for the GraphQL method with the given name.
     *
     * @param name      method name
     *
     * @return method result
     */
    public MethodResult getMethodResult(String name)
    {
        final Object value = data.get(name);
        final SchemaReference ref = findMethodRef(name);

        // we start a new hierarchy at the result type
        final SchemaReference resultRef = SchemaReference.newRef(domainQL, ref.getType().getName());
        return new MethodResult(resultRef, value);
    }


    /**
     * Finds the correct reference for the given GraphQL query or mutation name.
     *
     * @param name      query or mutation name
     *
     * @return reference to the method field
     */
    public SchemaReference findMethodRef(String name)
    {
        final GraphQLSchema schema = domainQL.getGraphQLSchema();
        final GraphQLObjectType queryType = (GraphQLObjectType) schema.getType("QueryType");

        final GraphQLFieldDefinition queryDef = queryType.getFieldDefinition(name);
        if (queryDef != null)
        {
            final GraphQLUnmodifiedType unwrapped = GraphQLTypeUtil.unwrapAll(queryDef.getType());
            return SchemaReference.newRef(
                domainQL,
                unwrapped.getName()
            );
        }

        final GraphQLObjectType mutationType = (GraphQLObjectType) schema.getType("MutationType");
        final GraphQLFieldDefinition mutationDef = mutationType.getFieldDefinition(name);
        if (mutationDef != null)
        {
            final GraphQLUnmodifiedType unwrapped = GraphQLTypeUtil.unwrapAll(mutationDef.getType());
            return SchemaReference.newRef(
                domainQL,
                unwrapped.getName()
            );
        }

        throw new GraphQLExportException("Could not find GraphQL method with name = " + name);
    }

    public record MethodResult(SchemaReference methodReference, Object methodResult)
    {
    }
}
