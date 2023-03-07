package de.quinscape.automaton.runtime.gzip;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Mini resource abstraction coming in two flavours: backed by a real file or only a stream
 */
final class ResourceHandle
{
    private final static Logger log = LoggerFactory.getLogger(ResourceHandle.class);

    private final String name;

    private final boolean workDirIsTemp;

    private final boolean noUpdates;

    /**
     * File backing this resource. Will always be null for stream-based resources
     */
    private volatile File backingFile;

    /**
     * Whether we have a backing file or not. Will be {@code null} if we have not checked yet.
     */
    private volatile Boolean haveFile;

    private final ServletContext servletContext;


    ResourceHandle(HttpServletRequest request, String name, boolean workDirIsTemp, boolean noUpdates)
    {
        this.servletContext = request.getServletContext();
        this.name = name;
        this.workDirIsTemp = workDirIsTemp;
        this.noUpdates = noUpdates;
    }

    private File getBackingFile()
    {
        if (haveFile == null)
        {
            synchronized (this)
            {
                if (haveFile == null)
                {
                    final String realPath = servletContext.getRealPath(name);
                    final boolean haveFile = realPath != null;

                    this.backingFile = haveFile ? new File(realPath) : null;
                    this.haveFile = haveFile;
                }
            }
        }
        return backingFile;
    }


    public boolean isModified(File ref)
    {
        File backingFile = getBackingFile();
        if (backingFile == null)
        {
            // stream resource -> did not change
            return false;
        }
        return backingFile.lastModified() > ref.lastModified();
    }


    public InputStream getInputStream()
    {
        final InputStream in = servletContext.getResourceAsStream(name);

        if (in == null)
        {
            throw new ResourceNotFoundException("No servlet resource stream for " + name);
        }

        return in;
    }


    public void checkForUpdate(File compressed) throws IOException
    {
        // we need to create a compressed copy if ...
        if (
            // no copy exists or ...
            !compressed.exists() ||
            // if we have modifications on the original resource (and we're not generally prohibited from updating)
            (!noUpdates && isModified(compressed))
        )
        {
            compress(compressed);
        }
    }

    private void compress(File output) throws IOException
    {
        log.debug("compress: {} => {}", this, output);

        final int bufferSize = 8192;

        try (

            InputStream in = getInputStream();
            OutputStream out =
                new GZIPOutputStream(
                    new FileOutputStream(output),
                    bufferSize,
                    false
                )
        )
        {
            IOUtils.copy(in, out, bufferSize);
        }
        finally
        {
            if (workDirIsTemp)
            {
                output.deleteOnExit();
            }
        }
    }
}
