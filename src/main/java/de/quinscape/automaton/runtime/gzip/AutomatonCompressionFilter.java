package de.quinscape.automaton.runtime.gzip;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * Automaton compression filter for dynamic content. T
 */
public class AutomatonCompressionFilter
    implements Filter
{
    private final static Logger log = LoggerFactory.getLogger(AutomatonCompressionFilter.class);

    @Override
    public void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain chain
    ) throws IOException, ServletException
    {
        filterInternal((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    public void filterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws IOException, ServletException
    {
        if (GzipUtil.supportsGzip(request))
        {
            log.debug("Preparing content caching wrapper for: {}", request.getRequestURI());

            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

            chain.doFilter(request, responseWrapper);


            FastByteArrayOutputStream baos = new FastByteArrayOutputStream(1024);
            GZIPOutputStream zip = new GZIPOutputStream(baos);
            IOUtils.copy(responseWrapper.getContentInputStream(), zip);
            zip.close();
            response.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
            response.setHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);
            response.setContentLength(baos.size());
            IOUtils.write(baos.toByteArray(), response.getOutputStream());
        }
        else
        {
            chain.doFilter(request, response);
        }
    }
}
