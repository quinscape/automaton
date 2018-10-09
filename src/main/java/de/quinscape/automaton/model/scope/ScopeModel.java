package de.quinscape.automaton.model.scope;

import java.util.List;

public class ScopeModel
{
    private List<ScopeDeclaration> declarations;

    public List<ScopeDeclaration> getDeclarations()
    {
        return declarations;
    }


    public void setDeclarations(List<ScopeDeclaration> declarations)
    {
        this.declarations = declarations;
    }
}
