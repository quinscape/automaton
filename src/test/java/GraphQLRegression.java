import graphql.AssertException;
import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphQLRegression
{
    private final static Logger log = LoggerFactory.getLogger(GraphQLRegression.class);


    @Ignore
    @Test(expected = AssertException.class)
    public void testDoubleDeclaration()
    {
        final GraphQLSchema.Builder builder = GraphQLSchema.newSchema();

        final GraphQLObjectType.Builder objectBuilder = GraphQLObjectType.newObject()
            .name("TargetFour")
            .field(
                GraphQLFieldDefinition.newFieldDefinition()
                .name("sourceFour")
                .type(Scalars.GraphQLString)
            )
            .field(
                GraphQLFieldDefinition.newFieldDefinition()
                .name("sourceFour")
                .type(Scalars.GraphQLString)
            );

        builder.query(
            GraphQLObjectType.newObject()
                .name("QueryType")
            .build()
        );
        builder.mutation(
            GraphQLObjectType.newObject()
                .name("MutationType")
            .build()
        );

        builder.additionalType(objectBuilder.build());


        log.info("SCHEMA: {}", builder.build());
    }
}
