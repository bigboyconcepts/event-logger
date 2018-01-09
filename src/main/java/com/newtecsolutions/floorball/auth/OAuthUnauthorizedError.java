package com.newtecsolutions.floorball.auth;

/**
 * Created by pedja on 20.9.16. 09.32.
 * This class is part of the Floorball
 * Copyright © 2016 ${OWNER}
 */
public class OAuthUnauthorizedError extends RuntimeException
{
    public OAuthUnauthorizedError()
    {
    }

    public OAuthUnauthorizedError(String message)
    {
        super(message);
    }

    public OAuthUnauthorizedError(String message, Throwable cause)
    {
        super(message, cause);
    }

    public OAuthUnauthorizedError(Throwable cause)
    {
        super(cause);
    }

    public OAuthUnauthorizedError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
