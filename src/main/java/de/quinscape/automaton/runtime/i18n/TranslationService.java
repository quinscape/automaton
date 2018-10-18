package de.quinscape.automaton.runtime.i18n;

import de.quinscape.domainql.util.JSONHolder;

public interface TranslationService
{
    JSONHolder getTranslations(String locale, String processName);
    void flush();
}
