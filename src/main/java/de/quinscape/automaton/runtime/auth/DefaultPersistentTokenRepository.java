package de.quinscape.automaton.runtime.auth;

import de.quinscape.spring.jsview.util.JSONUtil;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.svenson.util.JSONBeanUtil;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Persistent Token repository based on the app_user table
 */
public class DefaultPersistentTokenRepository<T>
    implements PersistentTokenRepository
{
    private final static Logger log = LoggerFactory.getLogger(DefaultPersistentTokenRepository.class);

    private static final String USERNAME = "username";

    private static final String TOKEN_VALUE = "token";

    private static final String SERIES = "series";

    private static final String LAST_USED = "lastUsed";

    private static final String FIELD_LAST_USED = "last_used";

    private final DSLContext dslContext;

    private final String table;

    private final Class<T> pojoClass;


    public DefaultPersistentTokenRepository(
        DSLContext dslContext,
        String table,
        Class<T> pojoClass
    )
    {
        this.dslContext = dslContext;
        this.table = table;
        this.pojoClass = pojoClass;
    }


    public void createNewToken(PersistentRememberMeToken token)
    {
        log.debug("createNewToken: {}", token);

        try
        {
            dslContext.insertInto(
                DSL.table(
                    DSL.name(
                        table
                    )
                )
            )
                .set(DSL.field(DSL.name(USERNAME)), token.getUsername())
                .set(DSL.field(DSL.name(TOKEN_VALUE)), token.getTokenValue())
                .set(DSL.field(DSL.name(SERIES)), token.getSeries())
                .set(DSL.field(DSL.name(FIELD_LAST_USED)), new Timestamp(token.getDate().getTime()))
                .execute();
        }
        catch (Exception e)
        {
            log.error("Error creating new token: " + token, e);
        }
    }


    public void updateToken(String series, String tokenValue, Date lastUsed)
    {
        log.debug("updateToken: {}, {}, {}", series, tokenValue, lastUsed);
        try
        {
            dslContext.update(
                DSL.table(
                    DSL.name(
                        table
                    )
                )
            )
                .set(DSL.field(DSL.name(TOKEN_VALUE)), tokenValue)
                .set(DSL.field(DSL.name(FIELD_LAST_USED)), new Timestamp(lastUsed.getTime()))
                .where(
                    DSL.field(
                        DSL.name(
                            SERIES
                        )
                    ).eq(
                        series
                    )
                )
                .execute();
        }
        catch (Exception e)
        {
            log.error("Error updating token: " + series + ", value = " + tokenValue + ", lastUsed = " + lastUsed, e);
        }
    }


    private Object findLoginForSeries(String series)
    {
        final List<T> logins = dslContext.select()
            .from(
                DSL.table(
                    DSL.name(
                        table
                    )
                )
            )
            .where(
                DSL.field(DSL.name(SERIES)).eq(series)
            )
            .fetchInto(pojoClass);

        if (logins.size() != 1)
        {
            return null;
        }

        return logins.get(0);
    }


    /**
     * Loads the token data for the supplied series identifier.
     * <p>
     * If an error occurs, it will be reported and null will be returned (since the result
     * should just be a failed persistent login).
     *
     * @param seriesId unique series identifier
     * @return the token matching the series, or null if no match found or an exception
     * occurred.
     */
    public PersistentRememberMeToken getTokenForSeries(String seriesId)
    {
        log.debug("getTokenForSeries: {}", seriesId);

        try
        {
            final Object login = findLoginForSeries(seriesId);
            if (login == null)
            {
                return null;
            }

            final JSONBeanUtil util = JSONUtil.DEFAULT_UTIL;

            final String usename = (String) util.getProperty(login, USERNAME);
            final String series = (String) util.getProperty(login, SERIES);
            final String tokenValue = (String) util.getProperty(login, TOKEN_VALUE);
            final Timestamp lastUsed = (Timestamp) util.getProperty(login, LAST_USED);

            return new PersistentRememberMeToken(
                usename,
                series,
                tokenValue,
                lastUsed
            );
        }
        catch (Exception e)
        {
            log.error("Error getting token for series" + seriesId, e);
            return null;
        }
    }


    public void removeUserTokens(String username)
    {
        log.debug("removeUserTokens: {}", username);

        dslContext.deleteFrom(
            DSL.table(
                DSL.name(
                    table
                )
            )
        ).where(
            DSL.field(DSL.name(USERNAME)).eq(username)
        );
    }
}
