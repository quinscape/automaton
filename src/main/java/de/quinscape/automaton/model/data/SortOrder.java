package de.quinscape.automaton.model.data;

import de.quinscape.automaton.runtime.InvalidSortOrderException;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Contains the list of field expressions to sort an interactive query by.
 */
public class SortOrder
{
    final static Pattern ORDER_BY_RE = Pattern.compile("^!?[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$");

    private List<String> fields;

    public SortOrder()
    {
        this(null);
    }

    public SortOrder(List<String> fields)
    {
        setFields(fields);
    }


    static List<String> validateSortOrder(List<String> fields)
    {
        if (fields == null)
        {
            return Collections.emptyList();
        }

        if (!fields.stream().allMatch(s -> ORDER_BY_RE.matcher(s).matches()))
        {
            throw new InvalidSortOrderException("Sort order contains invalid entries: " + fields);
        }
        return fields;
    }


    public void setFields(List<String> fields)
    {
        this.fields = validateSortOrder(fields);
    }


    /**
     * Field expressions. Either a field name or a field name prefixed with '!' for descending sort order.
     * 
     * @return
     */
    public List<String> getFields()
    {
        if (fields == null)
        {
            return Collections.emptyList();
        }
        return fields;
    }




}
