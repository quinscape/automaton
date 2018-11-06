package de.quinscape.automaton.runtime.controller;

import de.quinscape.automaton.runtime.provider.ProcessInjectionService;
import de.quinscape.automaton.runtime.util.ProcessUtil;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.svenson.util.JSONBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;


/**
 * Provides initialization data for processes and sub-processes started via AJAX requests.
 * <p>
 *     The initial root process is initialized within the initial page load and its initialization data is contained
 *     within the initial server data push.
 * </p>
 *
 * <p>
 *     Processes started via &lt;InternalLink/&gt; or as sub-processes use this controller to get their data.
 * </p>
 */
@Controller
public class ProcessController
{

    private final static Logger log = LoggerFactory.getLogger(ProcessController.class);


    private final ProcessInjectionService processInjectionService;


    @Autowired                                                                     
    public ProcessController(
        ProcessInjectionService processInjectionService
    )
    {
        this.processInjectionService = processInjectionService;
    }


    @RequestMapping(
        value = "/_auto/process/{appName}/{processName}",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> syncScope(
        HttpServletRequest request,
        @PathVariable("appName") String appName,
        @PathVariable("processName") String processName,
        @RequestBody String json
    ) throws IOException
    {
        final Map<String,Object> input = JSONUtil.DEFAULT_PARSER.parse(Map.class, json);

        final Map<String, Object> data = processInjectionService.getProcessInjections(
            appName,
            processName,
            input
        );

        return new ResponseEntity<>(
            JSONBuilder.buildObject(JSONUtil.DEFAULT_GENERATOR)
                .property("injections", data)
                .property("input", input)
                .output(),
            HttpStatus.OK
        );
    }
}
