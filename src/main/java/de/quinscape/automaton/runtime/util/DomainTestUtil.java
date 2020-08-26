package de.quinscape.automaton.runtime.util;

import de.quinscape.automaton.runtime.domain.op.DefaultStoreOperation;
import de.quinscape.automaton.runtime.domain.op.StoreOperation;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import org.apache.commons.io.FileUtils;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.util.RecastUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Helper class to load automaton JSON data into typed POJOs
 */
public class DomainTestUtil
{

    private final static Logger log = LoggerFactory.getLogger(DomainTestUtil.class);

    private final DomainQL domainQL;

    private final StoreOperation storeOperation;


    /**
     * Creates a new domain test util that uses the {@link DefaultStoreOperation}.
     *
     * @param domainQL      DomainQL instance
     * @param dslContext    DSL context
     */
    public DomainTestUtil(
        DomainQL domainQL,
        DSLContext dslContext
    )
    {
        this(domainQL, new DefaultStoreOperation(dslContext, domainQL));
    }


    /**
     * Creates a new domain test util with the given store operation.
     *
     * @param domainQL          DomainQL instance
     * @param storeOperation    store operation
     */
    public DomainTestUtil(
        DomainQL domainQL,
        StoreOperation storeOperation
    )
    {
        this.domainQL = domainQL;
        this.storeOperation = storeOperation;
    }


    /**
     * Loads the given entity JSON data and returns a list of typed domain object instances with converted scalar
     * properties.
     *
     * @param json JSON data containting an array of domain objects with "_type" field defining the type.
     *
     * @return list of typed domain objects
     */
    public List<DomainObject> load(String json)
    {
        final List<Map<String, Object>> raw = JSONUtil.DEFAULT_PARSER.parse(List.class, json);

        final List<DomainObject> domainObjects = new ArrayList<>(raw.size());

        for (Map<String, Object> rawObj : raw)
        {
            final String domainType = (String) rawObj.get("_type");

            final GraphQLType type = domainQL.getGraphQLSchema().getType(domainType);
            if (type instanceof GraphQLObjectType)
            {
                final List<GraphQLFieldDefinition> fieldDefs = ((GraphQLObjectType) type).getFieldDefinitions();
                for (GraphQLFieldDefinition fieldDef : fieldDefs)
                {
                    final GraphQLType fieldType = GraphQLTypeUtil.unwrapNonNull(fieldDef.getType());
                    if (fieldType instanceof GraphQLScalarType)
                    {
                        final String name = fieldDef.getName();
                        final Object value = rawObj.get(name);
                        if (value != null)
                        {
                            final Object converted = ((GraphQLScalarType) fieldType).getCoercing().parseValue(value);
                            rawObj.put(name, converted);
                        }
                    }
                }
            }
            else if (type instanceof GraphQLInputObjectType)
            {
                final List<GraphQLInputObjectField> fieldDefs =
                    ((GraphQLInputObjectType) type).getFieldDefinitions();

                for (GraphQLInputObjectField fieldDef : fieldDefs)
                {
                    final GraphQLType fieldType = GraphQLTypeUtil.unwrapNonNull(fieldDef.getType());
                    if (fieldType instanceof GraphQLScalarType)
                    {
                        final String name = fieldDef.getName();
                        final Object value = rawObj.get(name);
                        if (value != null)
                        {
                            final Object converted = ((GraphQLScalarType) fieldType).getCoercing().parseValue(value);
                            rawObj.put(name, converted);
                        }
                    }
                }
            }
            final DomainObject typed = (DomainObject) RecastUtil.recast(
                domainQL.getPojoType(domainType),
                rawObj,
                JSONUtil.OBJECT_SUPPORT
            );
            domainObjects.add(typed);
        }

        log.debug("Loaded domain objects: {}", domainObjects);

        return domainObjects;
    }


    /**
     * Inserts the given list of domain objects into the database using the configured store operation.
     *
     * @param domainObjects
     */
    public void insert(List<DomainObject> domainObjects)
    {
        for (DomainObject domainObject : domainObjects)
        {
            log.debug("Insert: {}", domainObject);

            storeOperation.execute(domainObject);
        }
    }


    /**
     * Convenience method to load the given JSON and store it in the database.
     *
     * @param json JSON data containing an array of domain objects.
     */
    public void loadAndInsert(String json)
    {
        final List<DomainObject> domainObjects = load(json);

        this.insert(
            domainObjects
        );
    }

    /**
     * Convenience method to loads JSON from the given file and store it in the database.
     *
     * @param file      file to load
     */
    public void loadAndInsert(File file) throws IOException
    {
        final List<DomainObject> domainObjects = load(FileUtils.readFileToString(file, "UTF-8"));

        this.insert(
            domainObjects
        );
    }
}
