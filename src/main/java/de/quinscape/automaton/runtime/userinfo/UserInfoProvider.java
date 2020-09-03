package de.quinscape.automaton.runtime.userinfo;

/**
 * Implemented by classes to provide additional information to a user based on the user id.
 */
public interface UserInfoProvider
{
    UserInfo provideUserInfo(String id);
}
