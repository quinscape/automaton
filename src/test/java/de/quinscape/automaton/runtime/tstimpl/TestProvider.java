package de.quinscape.automaton.runtime.tstimpl;

import de.quinscape.automaton.runtime.AutomatonException;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;

import java.sql.SQLException;
import java.util.Map;

/**
 * Implementation of {@link MockDataProvider} that defines results to be returned based on comparison of strings
 * containing the SQL statements expected.
 *
 * If no such query exists the an exception occurs.
 */
public class TestProvider
    implements MockDataProvider
{
    private final Map<String, TestResultProvider> testCases;

    private DSLContext dslContext;

    private TestProvider(Map<String, TestResultProvider> testCases)
    {
        this.testCases = testCases;
    }

    @Override
    public MockResult[] execute(MockExecuteContext ctx) throws SQLException
    {
        final String sql = ctx.sql();

        final TestResultProvider fn = testCases.get(sql);
        if (fn == null)
        {
            throw new AutomatonException("Could not find prepared result for SQL statement:\n" + sql);
        }

        return fn.provideResult(dslContext, ctx);
    }

    public static DSLContext create(Map<String, TestResultProvider> testCases)
    {
        return create(testCases, SQLDialect.POSTGRES);
    }


    private static DSLContext create(
        Map<String, TestResultProvider> testCases,
        SQLDialect sqlDialect
    )
    {
        final TestProvider provider = new TestProvider(testCases);
        MockConnection connection = new MockConnection(provider);
        final DSLContext dslContext = DSL.using(connection, sqlDialect);

        provider.setDSLContext(dslContext);
        return dslContext;
    }


    private void setDSLContext(DSLContext dslContext)
    {
        this.dslContext = dslContext;
    }
}
