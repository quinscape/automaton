package de.quinscape.automaton.runtime.userinfo;

/**
 * Wrapper for the actual user info payload which is dynamically typed/produced.
 */
public final class UserInfo
{
    private final String type;

    private final Object info;


    public UserInfo(String type, Object info)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("type can't be null");
        }

        if (info == null)
        {
            throw new IllegalArgumentException("info can't be null");
        }

        this.type = type;
        this.info = info;
    }


    /**
     * Returns the GraphQL object type of the payload
     *
     * @return GraphQL object type of the payload
     */
    public String getType()
    {
        return type;
    }


    /**
     * Returns the user info payload
     *
     * @return user info payload
     */
    public Object getInfo()
    {
        return info;
    }
}
