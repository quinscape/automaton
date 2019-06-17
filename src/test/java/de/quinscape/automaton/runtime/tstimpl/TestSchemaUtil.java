package de.quinscape.automaton.runtime.tstimpl;

import de.quinscape.domainql.DomainQL;
import graphql.schema.idl.SchemaPrinter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class TestSchemaUtil
{
    
    public static void writeGQLSchema(DomainQL domainQL) throws IOException
    {
        final SchemaPrinter schemaPrinter = new SchemaPrinter(
            SchemaPrinter.Options.defaultOptions()
                .includeScalarTypes(true)
        );
        FileUtils.writeStringToFile(
            new File("/home/sven/ideaprojects/automaton/schema.graphql"),
            schemaPrinter.print(domainQL.getGraphQLSchema()
            ),
            "UTF-8"
        );
    }

}
