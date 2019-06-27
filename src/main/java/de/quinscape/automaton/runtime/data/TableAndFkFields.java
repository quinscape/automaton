package de.quinscape.automaton.runtime.data;

import org.jooq.Field;
import org.jooq.Table;

import java.util.Set;

public final class TableAndFkFields
{
    private final Table<?> table;

    private final Set<Field<?>> fkFields;


    public TableAndFkFields(Table<?> table, Set<Field<?>> fkFields)
    {

        this.table = table;
        this.fkFields = fkFields;
    }


    public Table<?> getTable()
    {
        return table;
    }


    public Set<Field<?>> getFkFields()
    {
        return fkFields;
    }
}
