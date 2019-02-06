package de.quinscape.automaton.runtime.util;

import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;

public class SpringBeanUtil
{
    /**
     * Transforms a map of named spring beans by stripping of a fixed suffix off the keys of the map, should they
     * have such a suffix.
     *
     * @param mapIn     input map
     * @param suffix    string suffix
     * @param <T> bean type
     * @return new map
     */
    public static <T> Map<String,T> stripSuffix(Map<String,T> mapIn, String suffix)
    {
        Map<String, T> out = Maps.newHashMapWithExpectedSize(mapIn.size());
        for (Map.Entry<String, T> e : mapIn.entrySet())
        {
            final String name = e.getKey();

            final String newName;
            if (name.endsWith(suffix))
            {
                newName = name.substring(0, name.length() - suffix.length());
            }
            else
            {
                newName = name;
            }
            out.put(newName, e.getValue());
        }

        return Collections.unmodifiableMap(out);
    }
}
