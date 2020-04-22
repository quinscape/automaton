package de.quinscape.automaton.model.merge;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

public class MergeConfig
{
    private List<MergeTypeConfig> typeConfigs;

    private boolean allowDiscard = true;

    private boolean allowApply = true;


    @NotNull
    public List<MergeTypeConfig> getTypeConfigs()
    {
        if (typeConfigs == null)
        {
            return Collections.emptyList();
        }

        return typeConfigs;
    }


    public void setTypeConfigs(List<MergeTypeConfig> typeConfigs)
    {
        this.typeConfigs = typeConfigs;
    }


    public boolean isAllowDiscard()
    {
        return allowDiscard;
    }


    public void setAllowDiscard(boolean allowDiscard)
    {
        this.allowDiscard = allowDiscard;
    }


    public boolean isAllowApply()
    {
        return allowApply;
    }


    public void setAllowApply(boolean allowApply)
    {
        this.allowApply = allowApply;
    }
}
