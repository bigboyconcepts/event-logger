package com.newtecsolutions.floorball;

/**
 * Created by pedja on 20.9.16. 14.31.
 * This class is part of the Floorball
 * Copyright © 2016 ${OWNER}
 *
 * Exception that contains http status code and error code
 */
public class FBException extends RuntimeException
{
    private final int httpCode;
    private final MyResponse.ErrorCode errorCode;

    public FBException(MyResponse.ErrorCode errorCode, int httpCode, String message, Throwable throwable)
    {
        super(message, throwable);
        this.httpCode = httpCode;
        this.errorCode = errorCode;
    }

    public FBException(Throwable throwable)
    {
        super(throwable);
        this.httpCode = 500;
        this.errorCode = MyResponse.ErrorCode.server_error;
    }

    public FBException(String message)
    {
        super(message);
        this.httpCode = 500;
        this.errorCode = MyResponse.ErrorCode.server_error;
    }

    public FBException(MyResponse.ErrorCode errorCode, int httpCode)
    {
        this.httpCode = httpCode;
        this.errorCode = errorCode;
    }

    public FBException(int httpCode, String message)
    {
        super(message);
        this.httpCode = httpCode;
        this.errorCode = MyResponse.ErrorCode.server_error;
    }

    public FBException(MyResponse.ErrorCode errorCode, int httpCode, String message)
    {
        super(message);
        this.httpCode = httpCode;
        this.errorCode = errorCode;
    }

    public FBException(MyResponse.ErrorCode errorCode, String message)
    {
        super(message);
        this.httpCode = 500;
        this.errorCode = errorCode;
    }

    public int getHttpCode()
    {
        return httpCode;
    }

    public MyResponse.ErrorCode getErrorCode()
    {
        return errorCode;
    }
}
