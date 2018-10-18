package de.quinscape.automaton.runtime.util;

import java.util.Locale;

public final class LocaleUtil
{
    private static final String DEFAULT_LANGUAGE = "en-US";


    private LocaleUtil()
    {
    }

    public static String localeCode(Locale locale)
    {
        final String language = locale.getLanguage();
        final String country = locale.getCountry();
        if (country.length() == 0)
        {
            if (language.length() == 0)
            {
                return DEFAULT_LANGUAGE;
            }
            return language + "-" + language.toUpperCase();
        }
        else
        {
            return language + "-" + country.toUpperCase();
        }
    }
}
