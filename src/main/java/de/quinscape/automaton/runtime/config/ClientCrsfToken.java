package de.quinscape.automaton.runtime.config;

import org.springframework.security.web.csrf.CsrfToken;

public class ClientCrsfToken
{
    private final String param;
    private final String header;
    private final String value;

    public ClientCrsfToken(CsrfToken token)
    {
        param = token.getParameterName();
        header = token.getHeaderName();
        value = token.getToken();
    }


    public String getParam()
    {
        return param;
    }


    public String getHeader()
    {
        return header;
    }


    public String getValue()
    {
        return value;
    }
}
