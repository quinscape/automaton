package de.quinscape.automaton.runtime.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contains a standard request mapping for standalone view entry points that side-step the normal process and injection
 * mechanisms
 */
@Controller
public class ViewController
{
    /**
     * Map every name under /v/ to the entry point with the same name. This will by default only be the "v-login" view,
     * but you can add others.
     * 
     * @param viewName
     *
     * @return view name for JsV
     */
    @RequestMapping("/v/{viewName}/**")
    public String serveLogin(@PathVariable String viewName)
    {
        return viewName;
    }
}
