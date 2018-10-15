package de.quinscape.automaton.model.scope;

import de.quinscape.automaton.model.NamedModel;

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
        NamedModel.ensureUnique("Scope declarations", declarations);

        this.declarations = declarations;
    }
}
