package de.quinscape.automaton.runtime.config;

import de.quinscape.automaton.model.js.StaticFunctionReferences;
import de.quinscape.automaton.runtime.controller.GraphQLController;
import de.quinscape.automaton.runtime.i18n.DefaultTranslationService;
import de.quinscape.automaton.runtime.i18n.TranslationService;
import de.quinscape.automaton.runtime.logic.AutomatonStandardLogic;
import de.quinscape.automaton.runtime.provider.DefaultProcessInjectionService;
import de.quinscape.automaton.runtime.provider.ProcessInjectionService;
import de.quinscape.spring.jsview.loader.JSONResourceConverter;
import de.quinscape.spring.jsview.loader.ResourceHandle;
import de.quinscape.spring.jsview.loader.ResourceLoader;
import de.quinscape.spring.jsview.loader.ServletResourceLoader;
import graphql.GraphQL;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import javax.servlet.ServletContext;
import java.io.IOException;

/**
 * Standard bean definitions for automaton applications. The definition is incomplete in parts requiring the actual
 * application to provide the configured Spring beans
 *
 * <dl>
 *     <dt>
 *          DSLContext
 *     </dt>
 *     <dd>
 *         JOOQ/JTA configuration result
 *     </dd>
 *     <dt>
 *          GraphQLSchema
 *     </dt>
 *     <dd>
 *         Schema for the application
 *     </dd>
 *     <dt>
 *          GraphQL
 *     </dt>
 *     <dd>
 *         GraphQL instance, potentially instrumented
 *     </dd>
 * </dl>
 *
 */
@Configuration
@EnableWebSocket
@Import(WebsocketConfiguration.class)
public class AutomatonConfiguration
{
    private final ServletContext servletContext;


    @Autowired
    public AutomatonConfiguration(ServletContext servletContext)
    {
        this.servletContext = servletContext;
    }


    @Bean
    public AutomatonStandardLogic automatonStandardLogic(
        DSLContext dslContext
    )
    {
        return new AutomatonStandardLogic(dslContext);
    }

    @Bean
    public GraphQLController graphQLController(GraphQL graphQL)
    {
        return new GraphQLController(
            graphQL
        );
    }

    @Bean
    public ProcessInjectionService processInjectionService(
        GraphQL graphQL,
        ResourceHandle<StaticFunctionReferences> handle
    ) 
    {
        return new DefaultProcessInjectionService( handle, graphQL);
    }

    @Bean
    @Qualifier("jsFunctionReferences")
    public ResourceHandle<StaticFunctionReferences> jsFunctionReferencesHandle(
        ResourceLoader resourceLoader
    )
    {
        return resourceLoader.getResourceHandle(

            // Location where NPM "babel-plugin-track-usage" generates its JSON data
            "js/track-usage.json",

            // parse the resource into a StaticFunctionReferences instance
            new JSONResourceConverter<>(
                StaticFunctionReferences.class
            )
        );
    }

    @Bean
    public ResourceLoader resourceLoader() throws IOException
    {
        return new ServletResourceLoader(
            servletContext,
            "/",
            true
        );
    }

    @EventListener(ContextStoppedEvent.class)
    public void contextRefreshed(ContextStoppedEvent event) throws IOException
    {
        final ResourceLoader resourceLoader = resourceLoader();
        resourceLoader.shutDown();
    }
}
