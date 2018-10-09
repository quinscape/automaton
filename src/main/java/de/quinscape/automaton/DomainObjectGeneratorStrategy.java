package de.quinscape.automaton;

import de.quinscape.automaton.runtime.DomainObject;
import org.jooq.util.DefaultGeneratorStrategy;
import org.jooq.util.Definition;

import java.util.Collections;
import java.util.List;

/**
 * Makes all JOOQ POJOs extend
 */
public class DomainObjectGeneratorStrategy
    extends DefaultGeneratorStrategy
{
    @Override
    public List<String> getJavaClassImplements(
        Definition definition, Mode mode
    )
    {
        if ( mode == Mode.POJO)
        {
            return Collections.singletonList(DomainObject.class.getName());
        }
        return null;
    }
}
