package de.quinscape.automaton.model.merge;

public enum MergeResolutionFieldType
{
    /**
     * Normal input field
     */
    FIELD,
    /**
     * Embedded object for a foreign key
     */
    FK_OBJECT,
    /**
     * Embedded list of objects for a m-to-n relation
     */
    MANY_TO_MANY,

    /**
     * Foreign key raw key id field
     */
    FK_KEY,

    /**
     * Ignored field
     */
    IGNORE
}
