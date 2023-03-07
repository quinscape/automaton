package de.quinscape.automaton.runtime.gzip;

import de.quinscape.automaton.model.GzipPayload;
import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@GraphQLLogic
public class GzipTestLogic
{
    private final static Logger log = LoggerFactory.getLogger(GzipTestLogic.class);


    public static final String TEST_VALUE = "The quick brown fox jumps over the lazy dog. abcdefghijklmnopqrstuvwxyz";

    @GraphQLQuery
    public GzipPayload gzipPayload()
    {
        return new GzipPayload(TEST_VALUE);
    }
}
