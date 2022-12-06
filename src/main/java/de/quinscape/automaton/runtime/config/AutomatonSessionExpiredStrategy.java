package de.quinscape.automaton.runtime.config;

import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * A {@link SessionInformationExpiredStrategy} that ensures that AJAX requests with JSON content type don't get answered
 * by the standard plaintext error on session expiration.
 *
 * The strategy assumes a GraphQL like response structure with a "data" key containing the payload and an errors key
 * containing an array of GraphQL compatible error objects.
 *
 * When a session expires, this strategy responds with a single error with a {@link #SESSION_EXPIRED} message.
 */
public class AutomatonSessionExpiredStrategy
    implements SessionInformationExpiredStrategy
{

    private final static String SESSION_EXPIRED = "SESSION_EXPIRED";

    public static final String APPLICATION_JSON = "application/json";

    private final String sessionExpiredErrorJSON = "{\n" +
    "    \"data\" : null,\n" +
    "    \"errors\" : [{\n" +
    "        \"message\" : \"" + SESSION_EXPIRED + "\",\n" +
    "        \"path\" : []\n" +
    "    }]\n" +
    "}";

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException
    {
        final HttpServletRequest request = event.getRequest();

        final HttpServletResponse response = event.getResponse();

        if (Objects.equals(request.getContentType(), APPLICATION_JSON))
        {
            response.setContentType(APPLICATION_JSON);
            response.getWriter().print(sessionExpiredErrorJSON);
        }
        else {
            //default impl
            response.getWriter().print("This session has been expired.");
        }
        response.flushBuffer();
    }
}
