package com.newtecsolutions.floorball;


import org.skynetsoftware.jutils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;

/**
 * <pre>
 * Created by pedja on 16.9.16. 11.34.
 * This class is part of the Floorball
 * Copyright © 2016 ${OWNER}
 *
 * Wrapper for jax-rs {@link Response} handling error responses mostly
 * </pre>
 */

public class MyResponse
{
    public enum ErrorCode
    {
        server_error("Server Error", true),
        not_found("Not Found", false),
        invalid_input("Invalid Input", false),
        oauth_error("OAuth Error", false),
        api_error("Api Error", false),
        permission_error("permission Error", true),
        ;

        /**
         * Default error message string*/
        String message;

        /**
         * Determines if error can be logged*/
        private boolean isLoggable;

        ErrorCode(String message, boolean isLoggable)
        {
            this.message = message;
            this.isLoggable = isLoggable;
        }

        public boolean isLoggable()
        {
            return isLoggable;
        }
    }

    private List<Error> errors;
    private String responseMessage;
    private Object data;

    public MyResponse()
    {
        this.errors = new ArrayList<>();
    }

    public String getResponseMessage()
    {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage)
    {
        this.responseMessage = responseMessage;
    }

    public Object getData()
    {
        return data;
    }

    public MyResponse setData(Object data)
    {
        this.data = data;
        return this;
    }

    public List<Error> getErrors()
    {
        return errors;
    }

    public void setErrors(List<Error> errors)
    {
        this.errors = errors;
    }

    public void addError(ErrorCode errorCode, String errorMessage)
    {
        errors.add(new Error(errorCode, errorMessage));
    }

    /**
     * @see #errorResponse(ErrorCode, String, int) */
    public static Response errorResponse(ErrorCode errorCode)
    {
        return errorResponse(errorCode, null, 0);
    }

    /**
     * @see #errorResponse(ErrorCode, String, int) */
    public static Response errorResponse(ErrorCode errorCode, String message)
    {
        return errorResponse(errorCode, message, 0);
    }

    /**
     * Create error response. This creates jax-rs response
     * @param errorCode errorCode, cannot be null
     * @param message message that will override {@link ErrorCode#message}
     * @param httpCode http status code, if < 0, statu code 500 will be used*/
    public static Response errorResponse(@Nonnull ErrorCode errorCode, String message, int httpCode)
    {
        MyResponse pResponse = new MyResponse();
        pResponse.addError(errorCode, StringUtils.isEmpty(message) ? errorCode.message : message);
        return Response.status(httpCode <= 0 ? 500 : httpCode).entity(pResponse).build();
    }

    /**
     * Creates jax-rs {@link Response}*/
    public Response toResponse()
    {
        return Response.status(errors == null || errors.isEmpty() ? 200 : 500).entity(this).build();
    }

    /**
     * Wrapper for error, containing error code and error messages*/
    public static class Error
    {
        private ErrorCode errorCode;
        private String errorMessage;

        public Error(ErrorCode errorCode, String errorMessage)
        {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public ErrorCode getErrorCode()
        {
            return errorCode;
        }

        public void setErrorCode(ErrorCode errorCode)
        {
            this.errorCode = errorCode;
        }

        public String getErrorMessage()
        {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage)
        {
            this.errorMessage = errorMessage;
        }
    }
}
