package de.quinscape.automaton.runtime;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Map;
import java.util.TimeZone;

/**
 * GraphQL Scalar implementation for java.sql.Timestamp.
 */
public class GraphQLDomainObjectScalar
    extends graphql.schema.GraphQLScalarType

{
    private final DomainObjectFactory domainObjectFactory;

    public GraphQLDomainObjectScalar(DomainObjectFactory domainObjectFactory)
    {

        super("DomainObject", "Container for generic domain objects as scalar", new Coercing<DomainObject, Map<String,Object>>(){
            @Override
            public Map<String, Object> serialize(Object dataFetcherResult) throws CoercingSerializeException
            {

            }


            @Override
            public DomainObject parseValue(Object input) throws CoercingParseValueException
            {
                if (!(input instanceof Map))
                {
                    throw new CoercingParseValueException("Cannot coerce " + input + " to DomainObject");
                }


                final Map<String, Object> map = (Map<String, Object>) input;

                final String type = (String) map.get(DomainObject.DOMAIN_TYPE_PROPERTY);
                if (type == null)
                {
                    throw new
                }


                for (Map.Entry<String,Object> o : map.entrySet())
                {

                }


                return convert(isoString);
            }


            @Override
            public DomainObject parseLiteral(Object input) throws CoercingParseLiteralException
            {
                throw new CoercingParseLiteralException("Cannot coerce DomainObject from literal");
            }
        });

        this.domainObjectFactory = domainObjectFactory;
    }


    public static String toISO8601(Timestamp dataFetcherResult)
    {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);

        return df.format(dataFetcherResult);
    }


    private static Timestamp convert(String isoString)
    {
        Instant instant = Instant.parse(isoString);
        return new Timestamp(instant.toEpochMilli());
    }
}
