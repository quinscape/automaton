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
    private final int batchLimit;


    /**
     * Creates a new SequenceIdGenerator
     *
     * @param dslContext        JOOQ DSL context
     * @param sequence          JOOQ sequence to get values from
     * @param batchLimit        maximum number of id columns to fetch at once. (e.g. Postgresql limits SQL queries to 1604 columns)
     */
    public SequenceIdGenerator(DSLContext dslContext, Sequence<?> sequence, int batchLimit)
    {
        if (dslContext == null)
        {
            throw new IllegalArgumentException("dslContext can't be null");
        }

        if (sequence == null)
        {
            throw new IllegalArgumentException("sequence can't be null");
        }

        if (batchLimit <= 0)
        {
            throw new IllegalArgumentException("batchLimit must be larger than 0");
        }


        this.dslContext = dslContext;
        this.sequence = sequence;
        this.batchLimit = batchLimit;
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


    public int getBatchLimit()
    {
        return batchLimit;
    }


    @Override
    public List<Object> generate(@NotNull String domainType, int count)
    {
        if (domainType == null)
        {
            throw new IllegalArgumentException("domainType can't be null");
        }

        final List<Object> results = new ArrayList<>(count);
        do
        {
            final int numColumns = Math.min(batchLimit, count - results.size());
            final List<Field<?>> fields = new ArrayList<>();
            for (int i=0; i < numColumns ; i++)
            {
                final Field<?> nextval = sequence.nextval().as("id_" + i);
                fields.add(
                    nextval
                );
            }
            final Map<String, Object> resultMap = dslContext.select(fields).fetchAnyMap();
            results.addAll(resultMap.values());

        } while(results.size() < count);

        return results;
    }
}
