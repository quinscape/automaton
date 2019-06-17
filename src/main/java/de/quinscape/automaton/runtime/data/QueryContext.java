package de.quinscape.automaton.runtime.data;

import de.quinscape.domainql.DomainQL;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import org.jooq.Field;

import java.util.Collections;
import java.util.Map;

import static org.jooq.impl.DSL.*;

/**
 * Encapsulates the join table configuration of a
 */
public class QueryContext
{
    private final DataFetchingEnvironment env;

    private final DomainQL domainQL;

    private final Map<String, QueryJoin> joinAliases;
    private final Map<String, QueryJoin> joinAliasesRO;


    public QueryContext(
        DataFetchingEnvironment env,
        DomainQL domainQL,
        Map<String, QueryJoin> joinAliases
    )
    {
        this.env = env;
        this.domainQL = domainQL;
        this.joinAliases = joinAliases;
        this.joinAliasesRO = Collections.unmodifiableMap(joinAliases);
    }

    public Field<?> resolveField(String fieldName)
    {
        final SelectedField rowsField = env.getSelectionSet().getField("rows");
        final SelectedField field = rowsField.getSelectionSet().getField(fieldName.replace('.', '/'));
        final String parentLocation = getParent(field.getQualifiedName());
        final SelectedField parentField = getParentField(parentLocation);

        final Field<?> dbField = domainQL.lookupField(GraphQLTypeUtil.unwrapAll(parentField.getFieldDefinition()
            .getType()).getName(), field.getName());
        final String dbFieldName = dbField.getName();

        return field(
            name(
                joinAliases.get(parentLocation).getAlias(), dbFieldName
            ),
            dbField.getType()
        );
    }

    public static String getParent(String qualifiedName)
    {
        final int pos = qualifiedName.lastIndexOf('/');
        if (pos >= 0)
        {
            return qualifiedName.substring(0, pos);
        }

        return "";
    }

    public SelectedField getParentField(String parentLocation)
    {
        final SelectedField parentField;
        if (parentLocation.equals(""))
        {
            parentField = env.getSelectionSet().getField("rows");
        }
        else
        {
            parentField = env.getSelectionSet().getField("rows/" + parentLocation);
        }

        return parentField;
    }


    public Map<String, QueryJoin> getJoinAliases()
    {
        return joinAliasesRO;
    }
}
