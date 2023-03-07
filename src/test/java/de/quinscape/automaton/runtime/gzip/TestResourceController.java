package de.quinscape.automaton.runtime.gzip;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * This controller is a makeshift solution to the fact that for MockMvc requests, the root url maps to the class root, but
 * there is no controller mapping to actually serve the files. So we map this file controller to our top namespace to
 * be able to serve our test-resources from within MockMvc requests
 */
@Controller
public class TestResourceController
{
    @RequestMapping(value = "/de/**", produces = "application/javascript")
    public @ResponseBody String serveResource(
        HttpServletRequest request
    ) throws IOException
    {
        String path = request.getServletContext().getRealPath(request.getRequestURI());

        return FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8);
    }
}
