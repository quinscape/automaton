package de.quinscape.automaton.model.data;

import de.quinscape.automaton.runtime.InvalidSortOrderException;
import org.svenson.JSONParameter;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class SortOrder
{
    public static final SortOrder DEFAULT = new SortOrder(Collections.emptyList());

    final static Pattern ORDER_BY_RE = Pattern.compile("^!?[a-zA-Z_][a-zA-Z0-9_]*$");

    private final List<String> fields;


    public SortOrder(
        @JSONParameter("fields")
        List<String> fields
    )
    {
        this.fields = validateSortOrder(fields);
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


    public List<String> getFields()
    {
        return fields;
    }
}
