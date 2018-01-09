package com.newtecsolutions.floorball.input_validator;

/**
 * Created by pedja on 5.7.16. 15.29.
 * This class is part of the Iemand
 * Copyright © 2016 ${OWNER}
 */

public class InputValidatorError extends Exception
{
    public InputValidatorError()
    {
    }

    public InputValidatorError(String message)
    {
        super(message);
    }

    public InputValidatorError(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InputValidatorError(Throwable cause)
    {
        super(cause);
    }
}