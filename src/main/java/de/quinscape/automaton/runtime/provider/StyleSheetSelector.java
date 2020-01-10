package de.quinscape.automaton.runtime.provider;

import de.quinscape.spring.jsview.JsViewContext;

import java.util.List;

public interface StyleSheetSelector
{
    /**
     * Returns the default style sheet to use for the given js view context and the given list of system-defined
     * {@link StyleSheetDefinition}.
     *
     * @see AlternateStyleProvider
     *
     * @param context           js view context
     * @param styleSheets       system-defined {@link StyleSheetDefinition}
     * @return
     */
    StyleSheetDefinition select(JsViewContext context, List<StyleSheetDefinition> styleSheets);
}
