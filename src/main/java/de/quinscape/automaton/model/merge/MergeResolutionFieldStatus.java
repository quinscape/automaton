package de.quinscape.automaton.model.merge;

public enum MergeResolutionFieldStatus
{
    /**
     * User has not yet decided.
     */
    UNDECIDED,
    /**
     * We took the original value.
     */
    OURS,
    /**
     * We took over the current database value
     */
    THEIRS,

    /**
     * The user specified an alternate third value.
     */
    VALUE
}
