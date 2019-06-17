package de.quinscape.automaton.runtime.tstimpl;

import org.jooq.DSLContext;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;

public interface TestResultProvider
{
    MockResult[] provideResult(DSLContext dslContext, MockExecuteContext ctx);
}
