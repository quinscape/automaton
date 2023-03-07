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
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * A modern servlet filter that can be mapped to directories assumed to contain application resources which change never
 * or only in dev-mode.
 * <p>
 * A gzipped copy of all mapped files will be created on first access (while making sure other requests on the same
 * resource wait for it).
 * </p><p>
 * The gzipped copies then will be served via Tomcat's sendFile functionality if supported or with a simple fall-back
 * method.
 * </p><p>
 * This filter is *not* for dynamically generated output.
 * </p>
 *
 */
public final class AutomatonPreZipFilter
    implements Filter
{

    private final static String GZIP = ".gz";

    private final static Logger log = LoggerFactory.getLogger(AutomatonPreZipFilter.class);

    /**
     * Request attribute signalling sendFile support
     */
    public static final String SENDFILE_SUPPORT = "org.apache.tomcat.sendfile.support";

    /**
     * Request attribute specifying the file to send via Tomcat's sendFile
     */
    public static final String SENDFILE_FILENAME = "org.apache.tomcat.sendfile.filename";
    /**
     * Request attribute specifying the start offset of the file to send via Tomcat's sendFile
     */
    public static final String SENDFILE_START = "org.apache.tomcat.sendfile.start";
    /**
     * Request attribute specifying the end offset of the file to send via Tomcat's sendFile
     */
    public static final String SENDFILE_END = "org.apache.tomcat.sendfile.end";

    private final File workDir;
    private final boolean workDirIsTemp;

    private final ConcurrentMap<String, ResourceHandle> handles = new ConcurrentHashMap<>();

    private final boolean noUpdates;

    /**
     * Creates a new filter with the given working dir. The gzipped copies will be automatically updated if the webapp
     * is served from a file-based servlet context ("exploded WAR") and the modification date on the original resource
     * is younger than that of the gzipped copy.
     *
     * @param sWorkDir      path of the working directory. If empty, a temporary directory will be created.
     *
     * @throws IOException  when there's problems writing the temp directory
     */
    public AutomatonPreZipFilter(
        String sWorkDir
    ) throws IOException
    {
        this(sWorkDir, false);
    }


        /**
         * Creates a new filter with the given working dir
         *
         * @param sWorkDir      path of the working directory. If empty, a temporary directory will be created.
         * @param noUpdates     don't try to update out of date gzip data, even if the webapp is backed by file-based servlet context
         *
         * @throws IOException  when there's problems writing the temp directory
         */
    public AutomatonPreZipFilter(
        String sWorkDir,
        boolean noUpdates
    ) throws IOException
    {
        this.noUpdates = noUpdates;
        if (StringUtils.hasText(sWorkDir))
        {
            workDir = new File(sWorkDir);
            workDirIsTemp = false;
        }
        else
        {
            workDir = Files.createTempDirectory("automaton-prezip-").toFile();
            workDir.deleteOnExit();
            workDirIsTemp = true;
        }

        checkDir(workDir);

        log.debug("Starting AutomatonPreZipFilter with working dir = {}", workDir);
    }

    private static void checkDir(File tempDir)
    {
        if (tempDir.exists())
        {
            if (!tempDir.isDirectory())
            {
                throw new IllegalStateException("Not a directory: " + tempDir.getPath());
            }
        }
        else
        {
            if (!tempDir.mkdir())
            {
                throw new IllegalStateException("Could not create " + tempDir.getPath());
            }
        }
    }

    @Override
    public void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain filterChain
    ) throws IOException, ServletException
    {
        filterInternal(
            (HttpServletRequest) request,
            (HttpServletResponse) response,
            filterChain
        );
    }
    private void filterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws IOException, ServletException
    {
        if (GzipUtil.supportsGzip(request))
        {
            try
            {
                final String localURI = request.getRequestURI().substring(request.getContextPath().length());

                final String name = flatName(localURI);
                final ResourceHandle handle = getHandle(request, name, localURI);
                final File compressed = new File(workDir, name + GZIP);

                handle.checkForUpdate(compressed);

                response.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
                response.setHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);

                if (localURI.endsWith(".css"))
                {
                    response.setContentType("text/css");
                }
                else if (localURI.endsWith(".js"))
                {
                    response.setContentType("application/javascript");
                }
                else if (localURI.endsWith(".json") || localURI.endsWith(".map"))
                {
                    response.setContentType("application/json");
                }
                else
                {
                    response.setContentType("application/octet-stream");
                }
                response.setContentLength((int) compressed.length());

                if (supportsTomcatSendFile(request))
                {
                    // if we have support, use tomcat's sendFile functionality
                    request.setAttribute(SENDFILE_FILENAME, compressed.getCanonicalPath());
                    request.setAttribute(SENDFILE_START, 0L);
                    request.setAttribute(SENDFILE_END, compressed.length());
                }
                else
                {
                    // otherwise, send it ourselves
                    sendCompressed(response, compressed);
                }
            }
            catch (ResourceNotFoundException e)
            {
                response.sendError(HttpStatus.NOT_FOUND.value());
            }
        }
        else
        {
            filterChain.doFilter(request, response);
        }
    }


    /**
     * Fall-back method if Tomcat sendFile is not supported.
     *
     * @param response      response
     * @param compressed    compressed file
     * @throws IOException  if things go poof
     */
    private void sendCompressed(HttpServletResponse response, File compressed) throws IOException
    {
        try(
            FileInputStream in = new FileInputStream(compressed);
            OutputStream out = response.getOutputStream()
            )
        {
            IOUtils.copyLarge(in, out);
            out.flush();
        }
    }


    private boolean supportsTomcatSendFile(HttpServletRequest request)
    {
        final Boolean supported = (Boolean) request.getAttribute(SENDFILE_SUPPORT);
        final boolean isSupported = supported != null && supported;

        log.debug("sendFile supported: {}", isSupported);
        
        return isSupported;
    }


    private ResourceHandle getHandle(HttpServletRequest request, String name, String localURI)
    {
        final ResourceHandle handle = new ResourceHandle(request, localURI, workDirIsTemp, noUpdates);
        final ResourceHandle existing = handles.putIfAbsent(name, handle);
        if (existing != null)
        {
            return existing;
        }
        return handle;
    }




    private static String flatName(String uri)
    {
        return uri.replace('/', '_');
    }
}
