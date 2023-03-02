package de.quinscape.automaton.runtime.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Contains utility methods for exception handling
 */
public class ErrorUtil
{
    private ErrorUtil()
    {
        // no instances
    }


    /**
     * Returns the latest exception registered under the standard {@code "javax.servlet.error.exception"} request
     * attribute.
     *
     * @param request   Http servlet request
     *
     * @return exception or null if there is no exception registered as attribute
     */
    public static Throwable getLatestJavaxServletException(HttpServletRequest request)
    {
        final Object value = request.getAttribute("javax.servlet.error.exception");
        if (value instanceof Throwable)
        {
            return (Throwable) value;
        }

        return null;
    }


    /**
     * Searches the causes of the given throwable for an instance of a given class
     *
     * @param t         throwable to search causes from
     * @param cls       class that must match the cause
     *
     * @return true if the throwable has such a cause, false if not.
     */
    public static boolean hasCause(Throwable t, Class<? extends Throwable> cls)
    {
        if (t == null)
        {
            return false;
        }

        Throwable cause = null;
        do
        {
            cause = t.getCause();
            if (cause != null && cls.isAssignableFrom(cls))
            {
                return true;
            }

        } while (cause != null);
        return false;
    }
}
