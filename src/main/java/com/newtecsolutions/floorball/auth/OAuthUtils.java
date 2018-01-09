package com.newtecsolutions.floorball.auth;

import java.util.Arrays;
import java.util.Set;

/**
 * Created by pedja on 9/18/16.
 */

public class OAuthUtils
{
    public static String oauthScopesSetToString(Set<String> scopes)
    {
        return Arrays.toString(scopes.toArray(new String[scopes.size()]));
    }
}
