package de.quinscape.automaton.model.merge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

public class MergeConfig
{
    private final static Logger log = LoggerFactory.getLogger(MergeConfig.class);

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


    public MergeTypeConfig getTypeConfig(String domainType)
    {
        if (typeConfigs != null)
        {
            for (MergeTypeConfig typeConfig : typeConfigs)
            {
                if (typeConfig.getName().equals(domainType))
                {
                    return typeConfig;
                }
            }
        }

        log.debug("Using default config for type '{}'", domainType);

        return MergeTypeConfig.DEFAULT;
    }
}
