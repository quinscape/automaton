package de.quinscape.automaton.model.merge;

import javax.validation.constraints.NotNull;
import java.util.List;

public class MergeGroup
{
    private List<String> fields;


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
