package de.quinscape.automaton.runtime.provider;

import de.quinscape.automaton.model.js.StaticFunctionReferences;
import de.quinscape.automaton.runtime.auth.AutomatonAuthentication;
import de.quinscape.automaton.runtime.config.ClientCrsfToken;
import de.quinscape.automaton.runtime.i18n.TranslationService;
import de.quinscape.automaton.runtime.util.Base32;
import de.quinscape.automaton.runtime.util.LocaleUtil;
import de.quinscape.automaton.runtime.ws.AutomatonClientConnectionImpl;
import de.quinscape.automaton.runtime.ws.AutomatonWebSocketHandler;
import de.quinscape.spring.jsview.JsViewContext;
import de.quinscape.spring.jsview.JsViewProvider;
import de.quinscape.spring.jsview.loader.JSONResourceConverter;
import de.quinscape.spring.jsview.loader.ResourceConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.csrf.CsrfToken;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Provides the process injections and all necessary infrastructure data to execute a process on the client side.
 * 
 */
public final class AutomatonInjectionProvider
    implements JsViewProvider
{
    private final static Logger log = LoggerFactory.getLogger(AutomatonInjectionProvider.class);

    /**
     * Name under which automaton-js process expects the initial process data
     */
    private static final String INJECTIONS = "injections";

    /**
     * Location where NPM "babel-plugin-track-usage" generates its JSON data
     */
    private static final String USAGE_DATA_PATH = "js/track-usage.json";

    /**
     * Call name configured for call data to injection()
     */
    private static final String INJECTION_CALL_NAME = "injection";

    /**
     * Resource handle for the injection references that does automatic hot-reloading if the servlet context is deployed
     * from a file path (exploded WAR deployment).
     */
    private static final ResourceConverter<StaticFunctionReferences> CONVERTER = new JSONResourceConverter<>(
        StaticFunctionReferences.class
    );

    private static final String PROCESS_NAME = "processName";

    private static final String APP_NAME = "appName";


    private final ProcessInjectionService processInjectionService;

    private final boolean websocketEnabled;

    private final AutomatonWebSocketHandler automatonTestWebSocketHandler;

    private final TranslationService translationService;


    /**
     * Creates a new AutomatonInjectionProvider instance.
     * @param processInjectionService           project injection service
     * @param translationService
     * @param automatonTestWebSocketHandler     web socket handler, can be <code>null</code>
     */
    public AutomatonInjectionProvider(
        ProcessInjectionService processInjectionService,
        TranslationService translationService,
        AutomatonWebSocketHandler automatonTestWebSocketHandler
    )
    {
        if (processInjectionService == null)
        {
            throw new IllegalArgumentException("processInjectionService can't be null");
        }

        if (translationService == null)
        {
            throw new IllegalArgumentException("translationService can't be null");
        }

        this.processInjectionService = processInjectionService;
        this.websocketEnabled = automatonTestWebSocketHandler != null;
        this.automatonTestWebSocketHandler = automatonTestWebSocketHandler;
        this.translationService = translationService;
    }


    @Override
    public void provide(JsViewContext context) throws Exception
    {
        provideProcessInjections(context);
        provideCommonData(context);
    }


    private void provideCommonData(JsViewContext context)
    {
        final CsrfToken token = (CsrfToken) context.getRequest().getAttribute("_csrf");
        final AutomatonAuthentication auth = AutomatonAuthentication.current();

        context.provideViewData("contextPath", context.getRequest().getContextPath());
        context.provideViewData("authentication", auth);
        context.provideViewData("csrfToken", new ClientCrsfToken(token));

        if (websocketEnabled)
        {
            final String connectionId = Base32.uuid();
            context.provideViewData("connectionId", connectionId);

            automatonTestWebSocketHandler.register(
                new AutomatonClientConnectionImpl(
                    connectionId,
                    auth
                )
            );
        }
    }


    private void provideProcessInjections(JsViewContext context) throws java.io.IOException
    {
        final String processName = getProcessName(context);

        log.debug("Provide for process '{}'", processName);

        final Map<String, Object> injections = processInjectionService.getProcessInjections(processName);
        final String appName = context.getJsView().getEntryPoint();

        context.provideViewData(INJECTIONS, injections);
        context.provideViewData(PROCESS_NAME, processName);
        context.provideViewData(APP_NAME, appName);

        context.provideViewData("translations", translationService.getTranslations(LocaleUtil.localeCode(context.getRequest().getLocale()), appName + "/" + processName));
    }

    private String getProcessName(JsViewContext context)
    {
        final String uri = getURI(context);

        final int endOfFirst = uri.indexOf('/', 1);
        if (endOfFirst < 0)
        {
            throw new IllegalStateException("Local URI does not match /{app}/**");
        }
        final int length = uri.length();
        if (endOfFirst == length - 1)
        {
            return context.getJsView().getEntryPoint();
        }
        final int start = endOfFirst + 1;
        final int pos = uri.indexOf('/', start);
        if (pos < 0)
        {
            return uri.substring(start);
        }
        return uri.substring(start, pos);
    }


    private String getURI(JsViewContext context)
    {
        final HttpServletRequest request = context.getRequest();
        return request.getRequestURI().substring(request.getContextPath().length());
    }
}
