package com.newtecsolutions.floorball.auth;

import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.validators.OAuthValidator;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by pedja on 12/5/16 10:08 AM.
 * This class is part of the Floorball
 * Copyright © 2016 ${OWNER}
 */

public class MyOAuthTokenRequest extends OAuthTokenRequest
{
    /**
     * Create an OAuth Token request from a given HttpSerlvetRequest
     *
     * @param request the httpservletrequest that is validated and transformed into the OAuth Token Request
     * @throws OAuthSystemException  if an unexpected exception was thrown
     * @throws OAuthProblemException if the request was not a valid Token request this exception is thrown.
     */
    public MyOAuthTokenRequest(HttpServletRequest request) throws OAuthSystemException, OAuthProblemException
    {
        super(request);
    }

    @Override
    protected OAuthValidator<HttpServletRequest> initValidator() throws OAuthProblemException, OAuthSystemException
    {
        //validators.put(MemberResource.GRANT_TYPE_FACEBOOK, SocialLoginValidator.class);
        //validators.put(MemberResource.GRANT_TYPE_GOOGLE, SocialLoginValidator.class);
        return super.initValidator();
    }
}
