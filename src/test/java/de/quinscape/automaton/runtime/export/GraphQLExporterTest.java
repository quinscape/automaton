package de.quinscape.automaton.runtime.export;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class GraphQLExporterTest
{
    private final static Logger log = LoggerFactory.getLogger(GraphQLExporterTest.class);


    @Test
    void testNow()
    {
        String now = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
        assertThat(GraphQLExporter.replaceNow("[$now]"), is("[" + now + "]"));
        assertThat(GraphQLExporter.replaceNow("[\\$now]"), is("[$now]"));
        assertThat(GraphQLExporter.replaceNow("[\\\\$now]"), is("[\\\\" + now + "]"));
        assertThat(GraphQLExporter.replaceNow("[\\\\\\$now]"), is("[\\\\$now]"));
    }
}
