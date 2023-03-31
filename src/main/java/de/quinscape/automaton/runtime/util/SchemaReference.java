package de.quinscape.automaton.runtime.util;

import de.quinscape.domainql.DomainQL;
import de.quinscape.spring.jsview.util.JSONUtil;
import de.quinscape.spring.jsview.util.Util;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import org.svenson.util.JSONBeanUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a pointer to a type or field within a DomainQL type hierarchy. Each possible point in that hierarchy is
 * defined by a starting type (e.g. "Foo") and a path to get to the current element (e.g. "owner.login" which references
 * the login field of the owner object within the Foo type).
 *
 * The class creates parent and field / child references, provides information about the
 * current reference and fetches the data methodResult of the current reference from an object graph.
 *
 * @see #newRef(DomainQL, String)
 */
public class SchemaReference
{
    private final DomainQL domainQL;

    private final String rootType;

    private final List<String> path;

    private final GraphQLOutputType type;

    private final String dotPath;


    private SchemaReference(DomainQL domainQL, String rootType, List<String> path, String dotPathSource)
    {
        this.domainQL = domainQL;
        this.rootType = rootType;
        this.path = path;

        dotPath = dotPathSource != null ? dotPathSource : createDotPath(path);

        type = resolve(domainQL, rootType, path);
    }


    private static String createDotPath(List<String> path)
    {
        final StringBuilder sb = new StringBuilder();
        for (Iterator<String> iterator = path.iterator(); iterator.hasNext(); )
        {
            String s = iterator.next();
            sb.append(s);

            if (iterator.hasNext())
            {
                sb.append('.');
            }
        }

        return sb.toString();
    }


    /**
     * Returns true if the current schema reference points to a scalar type
     *
     * @return true if scalar type
     */
    public boolean isScalar()
    {
        return GraphQLTypeUtil.unwrapAll(this.type) instanceof GraphQLScalarType;
    }


    public boolean isRoot()
    {
        return path.size() == 0;
    }


    /**
     * Returns a reference to the parent object of the current reference or null if the current reference points the root object of
     * the hierarchy.
     *
     * @return parent reference or null if this reference is root.
     */
    public SchemaReference getParent()
    {
        if (isRoot())
        {
            return null;
        }
        else
        {
            return SchemaReference.newRef(domainQL, rootType, path.subList(0, path.size() - 1));
        }
    }


    /**
     * Returns a new schema reference that points to a field of the current path
     *
     * @param name      field name or path with dot notation (e.g. "owner.login")
     *
     * @return field path
     */
    public SchemaReference getField(String name)
    {
        return getField(Util.split(name, "."));
    }

    /**
     * Returns a new schema reference that points to a field of the current path
     *
     * @param path      List of path elements
     *
     * @return field path
     */
    public SchemaReference getField(List<String> path)
    {
        final List<String> p = new ArrayList<>(this.path);
        p.addAll(path);
        return SchemaReference.newRef(domainQL, rootType, p);
    }


    /**
     * Returns true if this path points to a non-null methodResult.
     *
     * @return true if non-null methodResult
     */
    public boolean isNonNull()
    {
        return type instanceof GraphQLNonNull;
    }


    /**
     * Returns true if the current path points to a list methodResult.
     *
     * @return true if list methodResult
     */
    public boolean isList()
    {
        return GraphQLTypeUtil.unwrapNonNull(type) instanceof GraphQLList;
    }


    /**
     * Returns the unwrapped type this schema reference points to
     *
     * @return type
     */
    public GraphQLUnmodifiedType getType()
    {
        return GraphQLTypeUtil.unwrapAll(type);
    }


    /**
     * Returns the unwrapped type this schema reference points to
     *
     * @return type
     */
    public GraphQLOutputType getOriginalType()
    {
        return type;
    }


    /**
     * Returns the type of the object parent of the current schema reference. If this is a root reference, the returned
     * type is identical to {@link #getType()}.
     *
     * @return object type
     */
    public GraphQLObjectType getObjectType()
    {
        if (isRoot())
        {
            return (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(type);
        }
        else
        {
            return (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(resolve(domainQL, rootType, path.subList(0, path.size() - 1)));
        }
    }


    /**
     * Resolves the raw GraphQL type resulting from traversing the given path starting at the given root type. This is mostly
     * meant for internal usage, but can be used to quickly retrieve a single type.
     *
     * @param graphQLSchema     schema
     * @param rootType          Name of the root type
     * @param path              path separated with dots (e.g. "name" / "owner.login")
     *
     * @return resolved raw/wrapped type
     */
    public static GraphQLOutputType resolve(DomainQL domainQL, String rootType, List<String> path)
    {
        final GraphQLSchema graphQLSchema = domainQL.getGraphQLSchema();
        final GraphQLType type = graphQLSchema.getType(rootType);

        if (type == null)
        {
            throw new SchemaReferenceException("Root type " + rootType + " does not exist");
        }
        if (!(type instanceof GraphQLObjectType))
        {
            throw new SchemaReferenceException("Root type " + rootType + " is no object type");
        }

        int pos = 0;

        GraphQLObjectType currentType = (GraphQLObjectType) type;
        GraphQLFieldDefinition currentField;
        while (pos < path.size())
        {

            final String fieldName = path.get(pos);
            currentField = currentType.getField(fieldName);
            pos++;

            if (currentField == null)
            {
                throw new SchemaReferenceException("Type " + currentType.getName() + " has no field '" + fieldName + "'");
            }

            final GraphQLOutputType fieldType = currentField.getType();
            if (pos == path.size())
            {
                return fieldType;
            }

            final GraphQLUnmodifiedType unwrapped = GraphQLTypeUtil.unwrapAll(fieldType);

            if (!(unwrapped instanceof GraphQLObjectType))
            {
                throw new SchemaReferenceException(
                    "Cannot resolve path starting at type " + rootType + ": " +
                    "Lookup of path " + JSONUtil.DEFAULT_GENERATOR.forValue(path) + " failed at position #" + pos
                );
            }

            currentType = (GraphQLObjectType) unwrapped;

        }
        return currentType;
    }

    /**
     * Returns the field or type meta methodResult with the given name, depending on what this schema reference points to.
     *
     * @param name      meta name
     * @return  meta methodResult
     * @param <T> expected meta methodResult type
     */
    public <T> T getMeta(String name)
    {
        final GraphQLObjectType objectType = getObjectType();
        if (isScalar())
        {
            return domainQL.getMetaData().getTypeMeta(objectType.getName()).getFieldMeta(path.get(path.size() - 1), name);
        }
        else
        {
            return domainQL.getMetaData().getTypeMeta(objectType.getName()).getMeta(name);
        }
    }


    public String getRootType()
    {
        return rootType;
    }


    public List<String> getPath()
    {
        return Collections.unmodifiableList(path);
    }


    /**
     * Resolves this schema reference within the given data object graph.
     * <p>
     *     Note that the schema reference mechanism works with GraphQL paths while the object graphs actually works
     *     on lodash-y paths. This method supports list fields by either returning the referenced list itself or by starting
     *     a new get() for every list field object. Accessing "AppUser.bazes" returns the list of Baz objects associated with
     *     the AppUser root. "AppUser.bazes.name" returns the list of names of all associated Baz objects.
     * </p>
     *
     * @param root      object graph
     *
     * @return value
     * @param <T> expected value type
     */
    public <T> T get(Object root)
    {
        final JSONBeanUtil beanUtil = new JSONBeanUtil();
        beanUtil.setObjectSupport(JSONUtil.OBJECT_SUPPORT);

        Object current = root;

        SchemaReference currentPath = SchemaReference.newRef(domainQL, rootType, Collections.emptyList());
        final int pathLen = path.size();
        final int last = pathLen - 1;
        for (int i = 0; i < pathLen; i++)
        {
            String p = path.get(i);
            final Object value = beanUtil.getProperty(current, p);

            if (value instanceof List)
            {
                if (i == last)
                {
                    return (T) value;
                }
                final SchemaReference subPath = SchemaReference.newRef(
                    domainQL,
                    GraphQLTypeUtil.unwrapAll(resolve(domainQL, rootType, path.subList(0, i + 1))).getName(),
                    path.subList(i + 1, pathLen)
                );

                List<?> out = new ArrayList<>();
                for (Object o : (List) value)
                {
                    out.add(
                        subPath.get(o)
                    );
                }
                return (T) out;
            }
            else
            {
                current = value;
                currentPath = currentPath.getField(p);
            }
        }
        return (T) current;
    }

    /**
     * Creates a new schema reference
     *
     * @param domainQL      DomainQL instance
     * @param rootType      root type
     * @param path          Path from the root type in dot notation (e.g. "owner.login")
     *
     * @return new schema reference
     */
    public static SchemaReference newRef(DomainQL domainQL, String rootType, String path)
    {
        return new SchemaReference(
            domainQL,
            rootType,
            Util.split(path, "."),
            path
        );
    }
    /**
     * Creates a new schema reference
     *
     * @param domainQL      DomainQL instance
     * @param rootType      root tyoe
     * @param path          Path from the root type in dot notation (e.g. "owner.login")
     *
     * @return new schema reference
     */
    public static SchemaReference newRef(DomainQL domainQL, String rootType, List<String> path)
    {
        return new SchemaReference(
            domainQL,
            rootType,
            path,
            null
        );
    }

    /**
     * Creates a new schema reference
     *
     * @param domainQL      DomainQL instance
     * @param rootType      root type
     *
     * @return new schema reference
     */
    public static SchemaReference newRef(DomainQL domainQL, String rootType)
    {
        return new SchemaReference(
            domainQL,
            rootType,
            Collections.emptyList(),
            ""
        );
    }


    @Override
    public String toString()
    {
        return super.toString() + ": " + this.rootType + (path.size() > 0 ?  "->" + dotPath : "");
    }


    /**
     * Returns the field name.
     *
     * @return field name or null if root
     */
    public String getFieldName()
    {
        if (isRoot())
        {
            return null;
        }

        return path.get(path.size() - 1);
    }

    public String toDotPath()
    {
        return dotPath;
    }
}
