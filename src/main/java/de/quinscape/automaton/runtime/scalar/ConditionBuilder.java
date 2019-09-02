package de.quinscape.automaton.runtime.scalar;

import de.quinscape.automaton.runtime.AutomatonException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to build {@link ConditionScalar} maps on the java side. Useful for tests and for pre-defining filters.
 */
public class ConditionBuilder
{
    private static String TYPE_FIELD = "type";
    /**
     * In fields, conditions and operations
     */
    private static String NAME_FIELD = "name";

    /**
     * The id field in components
     */
    private static String ID_FIELD = "id";

    /**
     * The condition field in components
     */
    private static String CONDITION_FIELD = "condition";

    /**
     * In conditions and operations
     */
    private static String OPERANDS_FIELD = "operands";
    /**
     * In Value
     */
    private static final String SCALAR_TYPE_FIELD = "scalarType";
    /**
     * In Value
     */
    private static final String VALUE_FIELD = "value";
    /**
     * In Values
     */
    private static final String VALUES_FIELD = "values";

    private static Class<? extends Map> mapImpl = HashMap.class;


    public static String getType(Map<String,Object> node)
    {
        return (String) node.get(TYPE_FIELD);
    }

    public static String getName(Map<String,Object> node)
    {
        assertType(node, NodeType.CONDITION, NodeType.OPERATION, NodeType.FIELD, NodeType.VALUE);
        return (String) node.get(NAME_FIELD);
    }

    public static List<Map<String, Object>> getOperands(Map<String,Object> node)
    {
        assertType(node, NodeType.CONDITION, NodeType.OPERATION);

        return (List<Map<String, Object>>) node.get(OPERANDS_FIELD);
    }

    public static String getScalarType(Map<String,Object> node)
    {
        assertType(node, NodeType.VALUE, NodeType.VALUES);

        return (String) node.get(SCALAR_TYPE_FIELD);
    }


    public static Object getValue(Map<String,Object> node)
    {
        assertType(node, NodeType.VALUE);

        return node.get(VALUE_FIELD);
    }

    public static Collection<?> getValues(Map<String,Object> node)
    {
        assertType(node, NodeType.VALUES);

        return (Collection<?>) node.get(VALUES_FIELD);
    }

    public static String getId(Map<String, Object> node)
    {
        assertType(node, NodeType.COMPONENT);

        return (String) node.get(ID_FIELD);
    }

    public static Map<String, Object> getCondition(Map<String, Object> node)
    {
        assertType(node, NodeType.COMPONENT);

        return (Map<String, Object>) node.get(CONDITION_FIELD);
    }

    public static void setType(Map<String,Object> node, String type)
    {
        node.put(TYPE_FIELD, type);
    }

    public static void setName(Map<String,Object> node, String name)
    {
        assertType(node, NodeType.CONDITION, NodeType.OPERATION, NodeType.FIELD, NodeType.VALUE);

        node.put(NAME_FIELD, name);
    }

    public static void setOperands(Map<String,Object> node, List<Map<String, Object>> operands)
    {
        assertType(node, NodeType.CONDITION, NodeType.OPERATION);
        node.put(OPERANDS_FIELD, operands);
    }

    public static void setScalarType(Map<String,Object> node, String scalarType)
    {
        assertType(node, NodeType.VALUE, NodeType.VALUES);

        node.put(SCALAR_TYPE_FIELD, scalarType);
    }


    public static void setValue(Map<String,Object> node, Object value)
    {
        assertType(node, NodeType.VALUE);

        node.put(VALUE_FIELD, value);
    }

    public static void setValues(Map<String,Object> node, Collection<?> values)
    {
        assertType(node, NodeType.VALUES);

        node.put(VALUES_FIELD, values);
    }

    public static void setId(Map<String, Object> node, String id)
    {
        assertType(node, NodeType.COMPONENT);

        node.put(ID_FIELD, id);
    }

    public static void setCondition(Map<String, Object> node, Map<String, Object> condition)
    {
        assertType(node, NodeType.COMPONENT);

        node.put(CONDITION_FIELD, condition);
    }


    private static void assertType(Map<String, Object> node, NodeType... types)
    {
        final String nodeType = ConditionBuilder.getType(node);
        for (NodeType type : types)
        {
            if (type.getName().equals(nodeType))
            {
                return;
            }
        }

        throw new AutomatonException("Unexpected node type: should be one of " + Arrays.asList(types) + ", is: " + nodeType);
    }


    /**
     * Creates a condition node
     *
     * @param name          condition name
     * @param operands      var args of conditions or fields/values
     *
     * @return condition type node
     */
    public static Map<String, Object> condition(String name, List<Map<String,Object>> operands)
    {
        final Map<String, Object> out = createMap();
        setType(out, NodeType.CONDITION.getName());
        setName(out, name);
        if (operands != null && operands.size() > 0)
        {
            setOperands(out, operands);
        }
        return out;
    }


    /**
     * Creates a component node
     *
     * @param id                component id
     * @param condition         condition for component
     *
     * @return component type node
     */
    public static Map<String, Object> component(String id, Map<String, Object> condition)
    {
        final Map<String, Object> out = createMap();
        setType(out, NodeType.COMPONENT.getName());
        setId(out, id);
        setCondition(out, condition);
        return out;
    }

    /**
     * Creates an or condition node
     *
     * @param operands      var args of conditions or fields/values
     *
     * @return condition type node
     */
    public static Map<String, Object> or(List<Map<String,Object>> operands)
    {
        return condition("or", operands);
    }

    /**
     * Creates an or condition node
     *
     * @param operands      var args of conditions or fields/values
     *
     * @return condition type node
     */
    public static Map<String, Object> and(List<Map<String,Object>> operands)
    {
        return condition("and", operands);
    }


    /**
     * Create a operation reference node encapsulating a scalar value
     *
     * @param name          operation name
     * @param operands      var args of fields
     *
     * @return operation type node
     */
    public static Map<String, Object> operation(String name, List<Map<String,Object>> operands)
    {
        final Map<String, Object> out = createMap();
        setType(out, NodeType.OPERATION.getName());
        setName(out, name);
        if (operands != null && operands.size() > 0)
        {
            setOperands(out, operands);
        }
        return out;
    }


    static Map<String, Object> createMap()
    {
        try
        {
            return mapImpl.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            throw new AutomatonException(e);
        }
    }


    /**
     * Create a field reference node encapsulating a scalar value
     *
     * @param name  field name (e.g. "name", "parent.name"))
     *
     * @return  field type node
     */
    public static Map<String, Object> field(String name)
    {
        final Map<String, Object> out = createMap();
        setType(out ,NodeType.FIELD.getName());
        setName(out, name);
        return out;
    }


    /**
     * Create a value node encapsulating a scalar value
     *
     * @param scalarType    scalar type name
     * @param value         value
     *
     * @return value type node
     */
    public static Map<String, Object> value(String scalarType, Object value)
    {
        return value(scalarType, value, null);
    }

    /**
     * Create a value node encapsulating a scalar value
     *
     * @param scalarType    scalar type name
     * @param value         value
     * @param name          field name
     *
     * @return value type node
     */
    public static Map<String, Object> value(String scalarType, Object value, String name)
    {
        final Map<String, Object> out = createMap();
        setType(out, NodeType.VALUE.getName());
        setScalarType(out, scalarType);
        setName(out, name);
        setValue(out, value);
        return out;
    }

    /**
     * Create a values node encapsulating a scalar value
     *
     * @param scalarType    scalar type name
     * @param values        values
     *
     * @return value type node
     */
    public static Map<String, Object> value(String scalarType, Collection<?> values)
    {
        final Map<String, Object> out = createMap();
        setType(out, NodeType.VALUES.getName());
        setScalarType(out, scalarType);
        setValues(out, values);
        return out;
    }

    /**
     * Create a condition scalar value for the given nested node data.
     *
     * @param data              nested condition map nodes
     *
     * @return condition scalar
     */
    public static ConditionScalar scalar(Map<String, Object> data)
    {
        return new ConditionScalar(data);
    }


    /**
     * Configures the map implementation to use for the created node objects.
     *
     * @param cls       class implementing Map
     */
    public static void setMapImpl(Class<? extends Map> cls)
    {
        mapImpl = cls;
    }


}
