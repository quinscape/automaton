package de.quinscape.automaton.model;

import org.atteo.evo.inflector.English;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Container model that has a unique name.
 */
public interface NamedModel
    extends Model
{
    String getName();

    /**
     * Ensures that the given list of named models has uniquely named elements.
     *
     * @param description       Describes the location this list occurs for the error message
     * @param namedModels       list of named models
     *
     * @throws IllegalStateException if the list elements are not uniquely named.
     */
    static void ensureUnique(String description, List<? extends NamedModel> namedModels)
    {
        Set<String> names = new HashSet<>();

        for (NamedModel namedModel : namedModels)
        {
            final String name = namedModel.getName();
            if (!names.add(name))
            {
                throw new IllegalStateException(description + ": List of " + English.plural(namedModel.getClass().getSimpleName()) + " is not unique: '" + name + "' is used more than once.");
            }
        }
    }
}
