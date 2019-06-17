package de.quinscape.automaton.runtime.util;

public class NameUtil
{
    /**
     * Strips the given suffix off the given name if the name ends in that suffix.
     *
     * @param name      name
     * @param suffix    suffix
     * @return  name without suffix
     */
    public static String stripSuffix(String name, String suffix)
    {
        final String newName;
        if (name.endsWith(suffix))
        {
            newName = name.substring(0, name.length() - suffix.length());
        }
        else
        {
            newName = name;
        }
        return newName;
    }

}
