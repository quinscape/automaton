package de.quinscape.automaton.runtime.util;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.util.JSONBeanUtil;
import org.svenson.util.JSONBuilder;

import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * Helper class to serialize domain objects to JSON 
 */
public class DomainSerializationUtil
{

    private final static Logger log = LoggerFactory.getLogger(DomainSerializationUtil.class);

    private final DomainQL domainQL;


    /**
     * Creates a new domain serialization util 
     *
     * @param domainQL          DomainQL instance
     */
    public DomainSerializationUtil(
        DomainQL domainQL
    )
    {
        if (domainQL == null)
        {
            throw new IllegalArgumentException("domainQL can't be null");
        }

        this.domainQL = domainQL;
    }


    /**
     * Serializes the given list of domain objects using the GraphQL scalar conversions.
     *
     * @param domainObjects     Domain object
     *
     * @return JSON string
     */
    public String serializeList(List<? extends DomainObject> domainObjects)
    {
        final JSONBuilder b = JSONBuilder.buildArray();

        for (DomainObject domainObject : domainObjects)
        {
            b.objectElement();
            serializeInternal(b, domainObject);
            b.close();
        }
        return b.output();
    }


    /**
     * Serializes the given domain object using the GraphQL scalar conversions.
     *
     * @param domainObject      Domain object
     *                          
     * @return JSON string
     */
    public String serialize(DomainObject domainObject)
    {
        if (domainObject == null)
        {
            return "null";
        }

        final JSONBuilder b = JSONBuilder.buildObject();

        serializeInternal(b, domainObject);

        return b.output();
    }


    /**
     * Dumps the converted properties of the given domain object into the given JSON builder
     *
     * @param builder           JSON builder
     * @param domainObject      domain object
     */
    private void serializeInternal(JSONBuilder builder, DomainObject domainObject)
    {
        @NotNull final String domainTypeName = domainObject.getDomainType();
        final GraphQLType gqlType = domainQL.getGraphQLSchema().getType(domainTypeName);

        if (!(gqlType instanceof GraphQLObjectType))
        {
            throw new DomainSerializationException(domainTypeName + " is not an output object type");
        }

        final JSONBeanUtil util = JSONUtil.DEFAULT_UTIL;

        final GraphQLObjectType type = (GraphQLObjectType) gqlType;

        final List<GraphQLFieldDefinition> fieldDefs = type.getFieldDefinitions();

        builder.property(DomainObject.DOMAIN_TYPE_PROPERTY, domainTypeName);

        for (GraphQLFieldDefinition fieldDef : fieldDefs)
        {
            final GraphQLOutputType fieldType = (GraphQLOutputType) GraphQLTypeUtil.unwrapNonNull(fieldDef.getType());
            if (fieldType instanceof GraphQLScalarType)
            {
                final String fieldDefName = fieldDef.getName();
                final Object value = util.getProperty(domainObject, fieldDefName);

                final Object serialized = ((GraphQLScalarType) fieldType).getCoercing().serialize(value);

                builder.property(fieldDefName, serialized);
            }
        }
    }
}
