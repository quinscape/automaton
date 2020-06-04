package de.quinscape.automaton.runtime.merge;

import de.quinscape.domainql.config.RelationModel;

class ManyToManyRelation
{
    private final RelationModel leftSideRelation;
    private final RelationModel rightSideRelation;


    ManyToManyRelation(RelationModel leftSideRelation, RelationModel rightSideRelation)
    {
        this.leftSideRelation = leftSideRelation;
        this.rightSideRelation = rightSideRelation;
    }


    /**
     * Returns the relation that goes from the link type back to the type from which we came.
     *
     * @return left side relation
     */
    public RelationModel getLeftSideRelation()
    {
        return leftSideRelation;
    }


    /**
     * Returns the relation that goes from the link type to the associated type (which is not the type we came from)
     * 
     * @return
     */
    public RelationModel getRightSideRelation()
    {
        return rightSideRelation;
    }
}

