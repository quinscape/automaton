package de.quinscape.automaton.runtime.filter;

public class FilterContextTestTarget
{
    private final String userId;

    private final String login;

    private final String url;


    public FilterContextTestTarget(String userId, String login, String url)
    {
        this.userId = userId;
        this.login = login;
        this.url = url;
    }


    public String getUserId()
    {
        return userId;
    }


    public String getLogin()
    {
        return login;
    }


    public String getUrl()
    {
        return url;
    }
}
