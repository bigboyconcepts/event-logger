package com.newtecsolutions.floorball.auth;

import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.validators.AbstractValidator;

import javax.servlet.http.HttpServletRequest;

public class SocialLoginValidator extends AbstractValidator<HttpServletRequest>
{

    public SocialLoginValidator()
    {
        requiredParams.add(OAuth.OAUTH_GRANT_TYPE);
        //requiredParams.add(MemberResource.PARAM_ACCESS_TOKEN);

        enforceClientAuthentication = true;
    }

}