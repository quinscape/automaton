package de.quinscape.automaton.runtime.domain;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Sequence;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates new ids based on a JOOQ numerical sequence.
 */
public final class SequenceIdGenerator
    implements IdGenerator
{

    public final static long MAGIC_ID = 0L;

    private final DSLContext dslContext;
    private final Sequence<?> sequence;


    public SequenceIdGenerator(DSLContext dslContext, Sequence<?> sequence)
    {
        if (dslContext == null)
        {
            throw new IllegalArgumentException("dslContext can't be null");
        }

        if (sequence == null)
        {
            throw new IllegalArgumentException("sequence can't be null");
        }

        this.dslContext = dslContext;
        this.sequence = sequence;
    }

    @Override
    public Object getPlaceholderId(@NotNull String domainType)
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

        List<Field<?>> fields = new ArrayList<>();
        for (int i=0; i < count ; i++)
        {
            final Field<?> nextval = sequence.nextval();
            fields.add(
                nextval
            );
        }
        final Map<String, Object> resultMap = dslContext.select(fields).fetchAnyMap();
        return new ArrayList<>(resultMap.values());
    }


}
