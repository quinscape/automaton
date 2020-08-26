package de.quinscape.automaton.runtime.userinfo;

import de.quinscape.automaton.runtime.auth.AutomatonUserDetails;
import de.quinscape.domainql.util.JSONHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Listens to Spring events and loads additional user information when a new login happens.
 *
 */
public class UserInfoService
    implements ApplicationListener<InteractiveAuthenticationSuccessEvent>
{
    private final static Logger log = LoggerFactory.getLogger(UserInfoService.class);


    private final ConcurrentMap<String,Object> userInfos = new ConcurrentHashMap<>();

    private final UserInfoProvider provider;


    public UserInfoService(UserInfoProvider provider)
    {
        this.provider = provider;
    }


    @Override
    public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event)
    {
        final Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof AutomatonUserDetails)
        {
            AutomatonUserDetails userDetails = (AutomatonUserDetails) principal;
            final String id = userDetails.getId();
            final Object userInfo = provider.provideUserInfo(id);

            log.debug("Login from {}/id={}: Got user info = {}", userDetails.getUsername(), userDetails.getId(), userInfo);

            userInfos.put(
                id,
                new JSONHolder(
                    userInfo
                )
            );
        }
        else
        {
            log.warn("Unknown login success type: " + principal);
        }
    }

    public Object getUserInfo(String id)
    {
        return userInfos.get(id);
    }


}
