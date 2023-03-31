package de.quinscape.automaton.runtime.export.excel;

import de.quinscape.automaton.runtime.AutomatonException;

public class ExcelExportException
    extends AutomatonException
{
    private static final long serialVersionUID = 3237223962811465065L;


    public ExcelExportException(String message)
    {
        super(message);
    }


    public ExcelExportException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ExcelExportException(Throwable cause)
    {
        super(cause);
    }
}
