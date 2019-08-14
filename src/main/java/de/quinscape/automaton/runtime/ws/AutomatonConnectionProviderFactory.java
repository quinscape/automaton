package de.quinscape.automaton.runtime.ws;

import de.quinscape.domainql.param.ParameterProvider;
import de.quinscape.domainql.param.ParameterProviderFactory;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;

public final class AutomatonConnectionProviderFactory
    implements ParameterProviderFactory
{
    private final ApplicationContext applicationContext;


    public AutomatonConnectionProviderFactory(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }


    @Override
    public ParameterProvider createIfApplicable(Class<?> parameterClass, Annotation[] annotations) throws Exception
    {
        if (parameterClass.equals(AutomatonClientConnection.class))
        {
            return new AutomatonClientConnectionProvider(applicationContext.getBean(AutomatonWebSocketHandlerImpl.class));
        }
        return null;
    }
}
