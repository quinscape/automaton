package de.quinscape.automaton.model.data;

import de.quinscape.automaton.runtime.InvalidSortOrderException;
import org.jooq.SelectField;
import org.jooq.SortField;
import org.svenson.JSONProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.jooq.impl.DSL.*;

public class SortOrder
{
    final static Pattern ORDER_BY_RE = Pattern.compile("^!?[a-zA-Z_][a-zA-Z0-9_]*$");

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


    public List<String> getFields()
    {
        return fields;
    }


    @JSONProperty(ignore = true)
    public Collection<? extends SortField<?>> getJooqFields()
    {
        Collection<SortField<Object>> list = new ArrayList<>();
        for (String fieldName : fields)
        {
            if (fieldName.startsWith("!"))
            {
                list.add(field(fieldName.substring(1)).desc());
            }
            else
            {
                list.add(field(fieldName).asc());
            }
        }
        return list;
    }


}
