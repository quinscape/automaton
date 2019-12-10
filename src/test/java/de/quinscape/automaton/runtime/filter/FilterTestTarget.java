package de.quinscape.automaton.runtime.filter;

public class FilterTestTarget
{
    private String name;

    private int num;

    private boolean flag;

    public FilterTestTarget()
    {
        this(null, 0, false);
    }


    public FilterTestTarget(String name, int num, boolean flag)
    {
        this.name = name;
        this.num = num;
        this.flag = flag;
    }


    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public int getNum()
    {
        return num;
    }


    public void setNum(int num)
    {
        this.num = num;
    }


    public boolean isFlag()
    {
        return flag;
    }


    public void setFlag(boolean flag)
    {
        this.flag = flag;
    }
}
