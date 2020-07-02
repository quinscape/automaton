package de.quinscape.automaton.runtime.config;

import de.quinscape.automaton.runtime.controller.GraphQLController;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

public class AllowDevGraphQLAccess
    implements RequestMatcher
{
    private static final Set<String> DEFAULT_IGNORED_METHODS;
    static
    {
        Set<String> set = new HashSet<>();
        set.add("GET");
        set.add("HEAD");
        set.add("TRACE");
        set.add("OPTIONS");
        DEFAULT_IGNORED_METHODS = set;
    }

    private final boolean allowDevGraphQLAccess;

    private final Set<String> ignoredMethods;


    public AllowDevGraphQLAccess(boolean allowDevGraphQLAccess)
    {
        this(allowDevGraphQLAccess, DEFAULT_IGNORED_METHODS);
    }

    public AllowDevGraphQLAccess(boolean allowDevGraphQLAccess, Set<String> ignoredMethods)
    {
        this.allowDevGraphQLAccess = allowDevGraphQLAccess;
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
                allowDevGraphQLAccess &&
                request.getRequestURI().equals(request.getContextPath() + GraphQLController.GRAPHQL_DEV_URI)
            );
    }
}
