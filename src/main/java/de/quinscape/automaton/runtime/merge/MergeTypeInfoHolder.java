package de.quinscape.automaton.runtime.merge;

import de.quinscape.domainql.DomainQL;

class MergeTypeInfoHolder
{
    private final DomainQL domainQL;

    private final String domainType;

    private volatile MergeTypeInfo mergeTypeInfo;


    public MergeTypeInfoHolder(DomainQL domainQL, String domainType)
    {
        this.domainQL = domainQL;
        this.domainType = domainType;
    }

    public MergeTypeInfo getMergeTypeInfo()
    {
        if (mergeTypeInfo == null)
        {
            synchronized (this)
            {
                if (mergeTypeInfo == null)
                {
                    mergeTypeInfo = new MergeTypeInfo(domainQL, domainType);
                }
            }
        }
        return mergeTypeInfo;
    }
}
