package de.quinscape.automaton.model.domain;


/**
 * Contains standardized Automaton relation tags.
 */
public class AutomatonRelation
{
    private AutomatonRelation()
    {
        // no instances
    }


    /**
     * Marks a relation as being part of a many-to-many relation pair. Both relations start at the same link type and both
     * relations must be tagged with this tag.
     */
    public final static String MANY_TO_MANY = "ManyToMany";
}
