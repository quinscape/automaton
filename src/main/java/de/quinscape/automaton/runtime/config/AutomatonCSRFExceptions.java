package de.quinscape.automaton.runtime.config;

import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

public class AutomatonCSRFExceptions
    implements RequestMatcher
{
    private static final Set<String> DEFAULT_IGNORED_METHODS;

    public static final String DEV_SERVICES_PATH ="/_dev/";

    static
    {
        Set<String> set = new HashSet<>();
        set.add("GET");
        set.add("HEAD");
        set.add("TRACE");
        set.add("OPTIONS");
        DEFAULT_IGNORED_METHODS = set;
    }

    private final boolean csrfExceptions;

    private final Set<String> ignoredMethods;


    public AutomatonCSRFExceptions(boolean csrfExceptions)
    {
        this(csrfExceptions, DEFAULT_IGNORED_METHODS);
    }

    public AutomatonCSRFExceptions(boolean csrfExceptions, Set<String> ignoredMethods)
    {
        this.csrfExceptions = csrfExceptions;
        this.ignoredMethods = ignoredMethods;
    }


    @Override
    public boolean matches(HttpServletRequest request)
    {
        // we only protect POST requests
        if (ignoredMethods.contains(request.getMethod()))
        {
            return false;
        }

        // require all requests to be requested unless allowDevGraphQLAccess is set and the request is to the special dev graphql URI
        return
            !(
                csrfExceptions &&
                request.getRequestURI().startsWith(request.getContextPath() + DEV_SERVICES_PATH)
            );
    }
}
