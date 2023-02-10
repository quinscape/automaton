package de.quinscape.automaton.model.merge;

import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

public class MergeGroup
{
    private List<String> fields;


    public MergeGroup()
    {
        this(Collections.emptyList());
    }
    
    public MergeGroup(List<String> fields)
    {
        this.fields = fields;
    }


    @NotNull
    public List<String> getFields()
    {
        return fields;
    }


    public void setFields(List<String> fields)
    {
        this.fields = fields;
    }
}
