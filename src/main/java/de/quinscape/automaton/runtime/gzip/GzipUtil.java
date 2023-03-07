package de.quinscape.automaton.runtime.gzip;

import jakarta.servlet.http.HttpServletRequest;

public class GzipUtil
{
    /**
     * Returns true if the given request expresses support for gzip encoding
     *
     * @param request
     *
     * @return true if gzip encoding is accepted
     */
    public static boolean supportsGzip(HttpServletRequest request)
    {
        String accept = request.getHeader("Accept-Encoding");
        return (accept != null && accept.contains("gzip"));
    }
}
