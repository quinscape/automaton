package de.quinscape.automaton.model.js;

import org.svenson.JSONParameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates the detected static function calls within one module.
 *
 */
public class ModuleFunctionReferences
{
    /**
     * Call name configured for call data to injection()
     */
    public static final String INJECTION_CALL_NAME = "injection";

    public static final String I18N_CALL_NAME = "i18n";

    private final String module;

    private final List<String> requires;

    private final Map<String, List<List<?>>> calls;


    public ModuleFunctionReferences(
        @JSONParameter("module")
        String module,
        @JSONParameter("requires")
        List<String> requires,
        @JSONParameter("calls")
        Map<String, List<List<?>>> calls)
    {
        this.module = module;
        this.requires = requires;

        Map<String,Set<String>> map = new HashMap<>();
        this.calls = calls;
    }


    /**
     * Module name (without leading "./")
     * @return
     */
    public String getModule()
    {
        return module;
    }


    /**
     * Map of variable names mapping to
     * @return
     */
    public List<String> getRequires()
    {
        if (requires == null)
        {
            return Collections.emptyList();
        }
        return requires;
    }


    /**
     * Returns the list of call parameters for the given symbolic call name as defined in the babel plugin config.
     *
     * @param name
     * @return
     */
    public List<List<?>> getCalls(String name)
    {
        List<List<?>> calls = this.calls.get(name);
        if (calls == null)
        {
            return Collections.emptyList();
        }
        return calls;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "module = '" + module + '\''
            + ", requires = " + requires
            + ", calls = " + calls
            ;
    }
}
