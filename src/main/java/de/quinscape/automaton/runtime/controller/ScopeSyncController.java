package de.quinscape.automaton.runtime.controller;

import de.quinscape.automaton.runtime.config.ScopeTableConfig;
import de.quinscape.domainql.jsonb.JSONB;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.jooq.DSLContext;
import org.jooq.InsertQuery;
import org.jooq.UpdateQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.sql.SQLException;
import java.util.Map;

import static org.jooq.impl.DSL.*;

@Controller
public class ScopeSyncController
{

    private final static Logger log = LoggerFactory.getLogger(ScopeSyncController.class);

    public static final ResponseEntity<String> OK = new ResponseEntity<>("{\"ok\":true}", HttpStatus.OK);

    private final ScopeTableConfig scopeTableConfig;
    private final DSLContext dslContext;

    @Autowired
    public ScopeSyncController(
        ScopeTableConfig scopeTableConfig,
        DSLContext dslContext
    )
    {
        if (dslContext == null)
        {
            throw new IllegalArgumentException("dslContext can't be null");
        }

        if (scopeTableConfig == null)
        {
            throw new IllegalArgumentException("scopeTableConfig can't be null");
        }


        this.dslContext = dslContext;
        this.scopeTableConfig = scopeTableConfig;
    }


    @RequestMapping(value = "/_auto/sync/{kind}/{name}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> syncScope(
        @PathVariable("kind") String kind,
        @PathVariable("name") String name,
        @RequestBody String json
    ) throws SQLException
    {
        log.info("syncScope-{} : {} = {}" , kind, name, json);

        if (kind.equals("app"))
        {
            updateAppScope(name, json);
        }
        else if (kind.equals("user"))
        {
            updateUserScope(name, json);
        }
        else
        {
            return error("Invalid kind '" + kind + "'");
        }

        return OK;
    }


    private void updateAppScope(
        String appName,
        String json
    ) throws SQLException
    {
        JSONB jsonb = JSONUtil.DEFAULT_PARSER.parse(JSONB.class, json);

        int count = dslContext.fetchCount(
            dslContext.selectCount()
                .from(scopeTableConfig.getAppScopeTable())
            .where(
                field("name").eq(appName)
            )
        );


        if (count == 0)
        {
            dslContext.insertInto(scopeTableConfig.getAppScopeTable())
                .set(field("name"), appName)
                .set(field("scope"), jsonb)
                .execute();
        }
        else
        {
            dslContext.update(scopeTableConfig.getAppScopeTable())
                .set(field("scope"), jsonb)
                .where(
                    field("name").eq(appName)
                )
                .execute();
        }
    }

    private void updateUserScope(
        String userName,
        String json
    )
    {
        final Map<String,Object> scopeMap = JSONUtil.DEFAULT_PARSER.parse(Map.class, json);

        int count = dslContext.fetchCount(
            dslContext.selectCount()
                .from(scopeTableConfig.getAppScopeTable())
                .where(
                    field("name").eq(userName)
                )
        );


        if (count == 0)
        {
            // non-DSL JOOQ update
            final UpdateQuery<?> query = dslContext.updateQuery(scopeTableConfig.getUserScopeTable());

            query.addConditions(
                field("name").eq(userName)
            );



            for (Map.Entry<String, Object> e : scopeMap.entrySet())
            {
                final String name = e.getKey();
                final Object value = e.getValue();

                query.addValue(
                    field(name),
                    value
                );
            }

            query.execute();
        }
        else
        {

            // non-DSL JOOQ insert
            final InsertQuery<?> query = dslContext.insertQuery(scopeTableConfig.getUserScopeTable());

            for (Map.Entry<String, Object> e : scopeMap.entrySet())
            {
                final String name = e.getKey();
                final Object value = e.getValue();

                query.addValue(
                    field(name),
                    value
                );
            }
            
            query.execute();
        }
    }


    private ResponseEntity<String> error(String s)
    {
        return new ResponseEntity<>("{\"error\":"+ JSONUtil.DEFAULT_GENERATOR.quote(s) +"}", HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
