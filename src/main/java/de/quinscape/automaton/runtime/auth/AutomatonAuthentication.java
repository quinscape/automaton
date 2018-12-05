package de.quinscape.automaton.runtime.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Set;

/**
 * Limited view on the app user details for client consumption.
 * 
 */
public class AutomatonAuthentication
{
    /**
     * Login name for the anonymous account.
     */
    public static final String ANONYMOUS = "anonymous";

    /**
     * Anonymous role.
     */
    public static final String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";

    /** Make sure the anonymous DB-User has this magic id */
    public static final String ANONYMOUS_ID = "af432487-a1b1-4f99-96d4-3b8e9796c95a";

    /**
     * The one anonymous AutomatonAuthentication instance
     */
    private static final AutomatonAuthentication ANONYMOUS_AUTH;

    static {
        ANONYMOUS_AUTH = new AutomatonAuthentication(
            ANONYMOUS,
            Collections.singleton(ROLE_ANONYMOUS),
            ANONYMOUS_ID
        );
    }

    private final String login;
    private final Set<String> roles;

    private final String id;


    private AutomatonAuthentication(String login, Set<String> roles, String id)
    {
        this.login = login;
        this.roles = roles;
        this.id = id;
    }

    /**
     * Accesses the spring security context to get the current AutomatonUserDetails.
     * For anonymous users, {@link #ANONYMOUS_AUTH} auth is returned.
     * @return
     */
    public static AutomatonAuthentication current()
    {
        SecurityContext context = SecurityContextHolder.getContext();

        Authentication authentication = context.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AutomatonUserDetails)
        {
            final AutomatonUserDetails details = (AutomatonUserDetails) authentication.getPrincipal();
            return new AutomatonAuthentication(details.getUsername(), details.getRoles(), details.getId());
        }
        if (authentication != null && authentication.getPrincipal() instanceof AutomatonAuthentication)
        {
            return (AutomatonAuthentication) authentication.getPrincipal();
        }
        else
        {
            return ANONYMOUS_AUTH;
        }
    }


    public String getLogin()
    {
        return login;
    }


    public Set<String> getRoles()
    {
        return roles;
    }


    public String getId()
    {
        return id;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "login = '" + login + '\''
            + ", roles = " + roles
            + ", id = '" + id + '\''
            ;
    }
}
