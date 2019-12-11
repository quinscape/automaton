package de.quinscape.automaton.runtime.pubsub;

public class TestPayload
{
    private String name;
    private int num;


    public TestPayload()
    {
    }
    
    public TestPayload(String name, int num)
    {
        this.name = name;
        this.num = num;
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
}
