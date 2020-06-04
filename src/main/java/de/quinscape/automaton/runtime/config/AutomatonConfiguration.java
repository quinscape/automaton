package de.quinscape.automaton.runtime.config;

import de.quinscape.automaton.model.js.StaticFunctionReferences;
import de.quinscape.automaton.runtime.controller.GraphQLController;
import de.quinscape.automaton.runtime.controller.ProcessController;
import de.quinscape.automaton.runtime.controller.ScopeSyncController;
import de.quinscape.automaton.runtime.data.DefaultInteractiveQueryService;
import de.quinscape.automaton.runtime.data.FilterTransformer;
import de.quinscape.automaton.runtime.data.InteractiveQueryService;
import de.quinscape.automaton.runtime.domain.IdGenerator;
import de.quinscape.automaton.runtime.domain.op.BatchStoreOperation;
import de.quinscape.automaton.runtime.domain.op.StoreOperation;
import de.quinscape.automaton.runtime.filter.JavaFilterTransformer;
import de.quinscape.automaton.runtime.logic.AutomatonStandardLogic;
import de.quinscape.automaton.runtime.merge.MergeService;
import de.quinscape.automaton.runtime.provider.DefaultProcessInjectionService;
import de.quinscape.automaton.runtime.provider.ProcessInjectionService;
import de.quinscape.domainql.DomainQL;
import de.quinscape.spring.jsview.loader.JSONResourceConverter;
import de.quinscape.spring.jsview.loader.ResourceHandle;
import de.quinscape.spring.jsview.loader.ResourceLoader;
import de.quinscape.spring.jsview.loader.ServletResourceLoader;
import graphql.GraphQL;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
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
@EnableScheduling
@Import({
    WebsocketConfiguration.class
})
public class AutomatonConfiguration
{
    private final static Logger log = LoggerFactory.getLogger(AutomatonConfiguration.class);


    private final ServletContext servletContext;


    @Autowired
    public AutomatonConfiguration(
        ServletContext servletContext
    )
    {
        this.servletContext = servletContext;
    }


    @Bean
    public AutomatonStandardLogic automatonStandardLogic(
        DSLContext dslContext,
        @Lazy DomainQL domainQL,
        IdGenerator idGenerator,
        StoreOperation storeOperation,
        BatchStoreOperation batchStoreOperation,
        FilterTransformer filterTransformer,
        @Lazy @Autowired(required = false) MergeService mergeService
    )
    {
        return new AutomatonStandardLogic(
            dslContext,
            domainQL,
            idGenerator,
            storeOperation,
            batchStoreOperation,
            filterTransformer,
            mergeService
        );
    }

    @Bean
    public GraphQLController graphQLController(GraphQL graphQL)
    {
        return new GraphQLController(
            graphQL
        );
    }

    @Bean
    public ScopeSyncController scopeSyncController(
        ScopeTableConfig scopeTableConfig,
        DSLContext dslContext
    )
    {
        return new ScopeSyncController(
            scopeTableConfig,
            dslContext
        );
    }

    @Bean
    public ProcessController processController(
        ProcessInjectionService processInjectionService
    )
    {
        return new ProcessController(
            processInjectionService
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

    @Bean
    public InteractiveQueryService interactiveQueryService(
        @Lazy DomainQL domainQL,
        DSLContext dslContext,
        FilterTransformer defaultFilterTransformer
    )
    {
        return new DefaultInteractiveQueryService(
            domainQL, dslContext, defaultFilterTransformer
        );
    }
    
    @Bean
    public FilterTransformer filterTransformer()
    {
        return new FilterTransformer();
    }

    @Bean
    public JavaFilterTransformer javaFilterTransformer(
    )
    {
        return new JavaFilterTransformer();
    }

    @Bean
    public TaskScheduler taskScheduler(
        @Value("${automaton.scheduler.pool-size:3}") int poolSize
    ) {

        log.info("Creating ThreadPoolTaskScheduler: pool size = {}", poolSize);

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(poolSize);
        scheduler.setThreadNamePrefix("auto-task-");
        scheduler.setDaemon(true);

        return scheduler;
    }

    @EventListener(ContextStoppedEvent.class)
    public void onContextStopped(ContextStoppedEvent event) throws IOException
    {
        final ResourceLoader resourceLoader = resourceLoader();
        resourceLoader.shutDown();
    }

}
