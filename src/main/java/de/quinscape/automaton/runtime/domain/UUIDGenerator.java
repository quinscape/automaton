package de.quinscape.automaton.runtime.domain;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class UUIDGenerator
    implements IdGenerator
{
    public final static String MAGIC_ID = "";

    @Override
    public Object getPlaceholderId(String domainType)
    {
        if (domainType == null)
        {
            throw new IllegalArgumentException("domainType can't be null");
        }

        return MAGIC_ID;
    }


    @Override
    public List<Object> generate(@NotNull String domainType, int count)
    {
        if (domainType == null)
        {
            throw new IllegalArgumentException("domainType can't be null");
        }

        List<Object> list = new ArrayList<>(count);
        for(int i = 0; i < count; i++)
        {
            list.add(
                UUID.randomUUID().toString()
            );
        }
        return list;
    }
}
