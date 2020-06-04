package de.quinscape.automaton.model.merge;

/**
 * User selected operation for a merge
 */
public enum MergeResolutionOperation
{
    /**
     * The whole process is aborted and the current working set data is to be discarded.
     */
    DISCARD,

    /**
     * The store operation was canceled. The working set remains as-is.
     */
    CANCEL,
    /**
     * The merge resolutions are to be applied without executing the final store operation. The user can inspect the merge
     * result in the original form context.
     */
    APPLY,

    /**
     * Merge resolutions are applied to the working set and the final store operation is performed.
     */
    STORE
}
