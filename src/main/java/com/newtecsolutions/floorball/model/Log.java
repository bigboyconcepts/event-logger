package com.newtecsolutions.floorball.model;

import com.newtecsolutions.floorball.MyResponse;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Created by pedja on 2/1/17 2:41 PM.
 * This class is part of the Floorball
 * Copyright © 2017 ${OWNER}
 */

@Entity
public class Log extends HibernateModel
{
    private String stacktrace;
    private MyResponse.ErrorCode errorCode;
    private int httpCode;
    private String remoteAddress;
    private String userAgent;
    private String requestUri;

    @Column(length = 4000)
    public String getStacktrace()
    {
        return stacktrace;
    }

    public void setStacktrace(String stacktrace)
    {
        this.stacktrace = stacktrace;
    }

    @Enumerated(EnumType.STRING)
    public MyResponse.ErrorCode getErrorCode()
    {
        return errorCode;
    }

    public void setErrorCode(MyResponse.ErrorCode errorCode)
    {
        this.errorCode = errorCode;
    }

    public int getHttpCode()
    {
        return httpCode;
    }

    public void setHttpCode(int httpCode)
    {
        this.httpCode = httpCode;
    }

    public String getRemoteAddress()
    {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress)
    {
        this.remoteAddress = remoteAddress;
    }

    public String getUserAgent()
    {
        return userAgent;
    }

    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }

    public String getRequestUri()
    {
        return requestUri;
    }

    public void setRequestUri(String requestUri)
    {
        this.requestUri = requestUri;
    }
}
