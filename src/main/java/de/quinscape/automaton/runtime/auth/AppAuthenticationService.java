package de.quinscape.automaton.runtime.auth;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.jooq.impl.DSL.*;

/**
 * Fetches UserDetails based on our app_user table
 */
public class AppAuthenticationService<T>
    implements UserDetailsService
{
    private final DSLContext dslContext;

    private String userTable;

    private final Class<?> pojoClass;


    public AppAuthenticationService(
        DSLContext dslContext,
        String userTable,
        Class<T> pojoClass
    )
    {
        this.dslContext = dslContext;
        this.userTable = userTable;
        this.pojoClass = pojoClass;
    }




    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException
    {
        //noinspection unchecked
        final List<T> appUsers = (List<T>) dslContext.select()
                .from(
                    DSL.table(
                        DSL.name(
                            userTable
                        )
                    )
                )
                .where( field("login").eq(userName))
                .fetchInto(pojoClass);

        if (appUsers.size() == 0)
        {
            throw new UsernameNotFoundException("Could not find login with name '" + userName + "'");
        }
        if (appUsers.size() > 1)
        {
            throw new UsernameNotFoundException("Found more than one login with name '" + userName + "'");
        }
        return new AutomatonUserDetails(
            appUsers.get(0)
        );
    }
}
