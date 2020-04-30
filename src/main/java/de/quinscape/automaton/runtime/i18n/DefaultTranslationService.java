package de.quinscape.automaton.runtime.i18n;

import com.google.common.collect.Maps;
import de.quinscape.automaton.model.js.ModuleFunctionReferences;
import de.quinscape.automaton.model.js.StaticFunctionReferences;
import de.quinscape.automaton.runtime.AutomatonException;
import de.quinscape.domainql.util.JSONHolder;
import de.quinscape.spring.jsview.loader.ResourceHandle;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.util.JSONBeanUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jooq.impl.DSL.*;

public class DefaultTranslationService
    implements TranslationService
{
    private final static Logger log = LoggerFactory.getLogger(DefaultTranslationService.class);

    private static final JSONHolder EMPTY = new JSONHolder(Collections.emptyMap());

    // processName -> locale ->  translations per tag
    private volatile Map<String, Map<String, JSONHolder>> allTranslations;

    private final DSLContext dslContext;

    private final ResourceHandle<StaticFunctionReferences> handle;

    private final Table<?> translationTable;

    private final Class<?> translationClass;


    private final static Pattern PROCESS_MAIN_PATTERN = Pattern.compile("^\\./apps/(.*?)/processes/(.*?)/(.*?)$");

    public DefaultTranslationService(
        DSLContext dslContext,
        ResourceHandle<StaticFunctionReferences> handle,
        Table<?> translationTable,
        Class<?> translationClass
    )
    {
        this.dslContext = dslContext;
        this.handle = handle;
        this.translationTable = translationTable;
        this.translationClass = translationClass;
        allTranslations = updateTranslations();

    }


    private static <T> Map<String, T> newMap(String k)
    {
        return new HashMap<>();
    }


    private Map<String, Map<String, JSONHolder>> updateTranslations()
    {

        try
        {
            final Map<String, Map<String, JSONHolder>> allTranslations = new HashMap<>();
            final List<?> translations = dslContext
                .select()
                .from(translationTable)
                .orderBy(
                    field(
                        name("locale")
                    ),
                    field(
                        name("tag")
                    ),
                    field(
                        name("process_name")
                    )
                )
                .fetchInto(translationClass);

            final StaticFunctionReferences refs = handle.getContent();

            Set<String> processNames = findProcessNames(refs);
            final JSONBeanUtil util = JSONUtil.DEFAULT_UTIL;

            for (String currentProcess : processNames)
            {
                Map<String, Map<String,String>> processMap = new HashMap<>();

                for (Object row : translations)
                {
                    final String tag = (String) util.getProperty(row, "tag");
                    final String locale = (String) util.getProperty(row, "locale");
                    final String processName = (String) util.getProperty(row, "processName");
                    final String translation = (String) util.getProperty(row, "translation");

                    if (processName.length() == 0 || processName.equals(currentProcess))
                    {
                        Map<String,String> localeMap = processMap.computeIfAbsent(
                            locale,
                            DefaultTranslationService::newMap
                        );

                        localeMap.put(tag, translation);
                    }
                }


                allTranslations.put(currentProcess,
                    prepareProcessMap(currentProcess, processMap)
                );
            }


            return allTranslations;
        }
        catch (IOException e)
        {
            throw new AutomatonException(e);
        }

    }


    private final static Pattern LOCALE_PATTERN = Pattern.compile ("(.*?)-(.*?)");

    private Map<String, JSONHolder> prepareProcessMap(
        String currentProcess, Map<String, Map<String, String>> processMap
    )
    {
        final Map<String, JSONHolder> map = Maps.newHashMapWithExpectedSize(processMap.size());

        createFallbacks(processMap);


        for (Map.Entry<String, Map<String, String>> e : processMap.entrySet())
        {
            final String locale = e.getKey();
            final Map<String, String> localeMap = e.getValue();
            final JSONHolder holder = new JSONHolder(localeMap);

            if (log.isDebugEnabled())
            {
                log.debug("Translations for '{}', {} = {}", currentProcess, locale, JSONUtil.formatJSON(holder.toJSON()));
            }
            map.put(locale, holder);
        }
        return map;
    }


    private void createFallbacks(Map<String, Map<String, String>> processMap)
    {
        Map<String, Map<String, String>> fallbacks = new HashMap<>();

        for (Map.Entry<String, Map<String, String>> e : processMap.entrySet())
        {
            final String locale = e.getKey();
            final Map<String, String> localeMap = e.getValue();

            final Matcher matcher = LOCALE_PATTERN.matcher(locale);
            if (matcher.matches())
            {
                final String language = matcher.group(1);
                final String country = matcher.group(2);

                final String upperLANG = language.toUpperCase();
                if (!upperLANG.equals(country))
                {
                    final String fallback = language + "-" + upperLANG;
                    if (!processMap.containsKey(fallback))
                    {
                        fallbacks.put(fallback, localeMap);
                    }
                }
            }
        }

        processMap.putAll(fallbacks);
    }


    /**
     * Returns a set of qualified process names (e.g. "myapp/myprocess") from the available js modules.
     *
     * @param refs      js function references
     *
     * @return  set of qualified process names in the format "app/process"
     *
     * @throws IOException  if the js function references could not be read (only when hot-reloading)
     */
    private Set<String> findProcessNames(StaticFunctionReferences refs) throws IOException
    {
        Set<String> processNames = new HashSet<>();

        // handle may be hot-reloading, so we can't cache this
        final Map<String, ModuleFunctionReferences> refsMap = refs.getModuleFunctionReferences();

        if (refsMap != null)
        {
            for (Map.Entry<String, ModuleFunctionReferences> e : refsMap.entrySet())
            {
                final String modulePath = e.getKey();

                final Matcher matcher = PROCESS_MAIN_PATTERN.matcher(modulePath);

                if (matcher.matches())
                {
                    final String appName = matcher.group(1);
                    final String processName = matcher.group(2);
                    final String moduleName = matcher.group(3);

                    if (processName.equals(moduleName))
                    {
                        processNames.add(appName + "/" + processName);
                    }
                }
            }
        }
        return processNames;
    }


    public JSONHolder getTranslations(String locale, String qualifiedProcessName)
    {
        final Map<String, JSONHolder> processMap = allTranslations.get(qualifiedProcessName);

        if (processMap != null)
        {
            final JSONHolder holder = processMap.get(locale);
            if (holder != null)
            {
                return holder;
            }
        }


        return EMPTY;
    }


    @Override
    public void flush()
    {
        allTranslations = updateTranslations();
    }
}
