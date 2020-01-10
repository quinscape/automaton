package de.quinscape.automaton.runtime.provider;

import de.quinscape.domainql.util.JSONHolder;
import de.quinscape.spring.jsview.JsViewContext;
import de.quinscape.spring.jsview.JsViewProvider;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.svenson.util.JSONBuilder;

import javax.servlet.http.Cookie;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

/**
 * Provides a list of alternate stylesheets to the client and sets a base template placeholder <code>$CURRENT_STYLE</code>.
 *
 * By default the current style is either the first style provided. Alternatively, the provider can be constructed with
 * a {@link StyleSheetSelector} instance.
 *
 *
 */
public class AlternateStyleProvider
    implements JsViewProvider
{
    public final static String CURRENT_STYLE = "CURRENT_STYLE";
    public final static String PREFERRED_STYLE = "_AUTO_PREFERRED_STYLE";

    private final String contextPath;

    private final List<StyleSheetDefinition> styleSheets;

    private final String styleSheetsJSON;

    private final StyleSheetSelector selector;


    /**
     * Creates a new AlternateStyleProvider for the given context path and list of style sheet definitions.
     *
     * The provider will use the first stylesheet in the list as default.
     *
     * @param contextPath   contextPath
     * @param styleSheets   style sheet definitions
     */
    public AlternateStyleProvider(
        String contextPath,
        List<StyleSheetDefinition> styleSheets
    )
    {
        this(contextPath, styleSheets, null);
    }


    /**
     * Creates a new AlternateStyleProvider for the given context path, list of style sheet definitions and style sheet
     * selector
     *
     * @param contextPath   contextPath
     * @param styleSheets   style sheet definitions
     * @param selector      selector instance to select the default style sheet.
     */
    public AlternateStyleProvider(
        String contextPath,
        List<StyleSheetDefinition> styleSheets,
        StyleSheetSelector selector
    )
    {
        if (contextPath == null)
        {
            throw new IllegalArgumentException("contextPath can't be null");
        }

        if (styleSheets == null)
        {
            throw new IllegalArgumentException("styleSheets can't be null");
        }

        if (styleSheets.size() < 2)
        {
            throw new IllegalArgumentException("styleSheets has to have at least 2 style sheets");
        }

        this.contextPath = contextPath;
        this.styleSheets = styleSheets;
        this.styleSheetsJSON = JSONUtil.DEFAULT_GENERATOR.forValue(styleSheets);
        this.selector = selector;
    }


    @Override
    public void provide(JsViewContext context) throws Exception
    {
        final StyleSheetDefinition current;
        if (this.selector != null)
        {
            current = selector.select(context, styleSheets);
        }
        else
        {
            final StyleSheetDefinition styleFromCookie = readCookie(context);

            if (styleFromCookie != null)
            {
                current = styleFromCookie;
            }
            else
            {
                current = styleSheets.get(0);
            }
        }

        final JSONBuilder b = JSONBuilder.buildObject(JSONUtil.DEFAULT_GENERATOR);
        b.includeProperty("styleSheets", styleSheetsJSON);
        b.property("currentStyleSheet", current.getName());

        context.provideViewData("alternateStyles", new JSONHolder(b.output()));
        context.setPlaceholderValue(CURRENT_STYLE, contextPath + current.getUri());
    }

    private StyleSheetDefinition readCookie(JsViewContext context) throws UnsupportedEncodingException
    {
        final Cookie[] cookies = context.getRequest().getCookies();
        for (Cookie cookie : cookies)
        {
            if (cookie.getName().equals(PREFERRED_STYLE))
            {
                final String name = cookie.getValue();

                return findStyle(URLDecoder.decode(name, "UTF-8"));
            }
        }
        return null;
    }


    private StyleSheetDefinition findStyle(String name)
    {
        for (StyleSheetDefinition definition : styleSheets)
        {
            if (definition.getName().equals(name))
            {
                return definition;
            }
        }
        return null;
    }
}
