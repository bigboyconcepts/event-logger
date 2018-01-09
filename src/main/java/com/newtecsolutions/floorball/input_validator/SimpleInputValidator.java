package com.newtecsolutions.floorball.input_validator;

import com.newtecsolutions.floorball.utils.LocaleManager;

import org.skynetsoftware.jutils.JUtils;
import org.skynetsoftware.jutils.StringUtils;

import java.util.Locale;

import javax.inject.Inject;

/**
 * Created by pedja on 5.7.16. 15.32.
 * This class is part of the Iemand
 * Copyright © 2016 ${OWNER}
 */

public class SimpleInputValidator
{
    private LocaleManager localeManager;

    @Inject
    public SimpleInputValidator(LocaleManager localeManager)
    {
        this.localeManager = localeManager;
    }

    /**
     * Check if empty
     * @param field field parameter, for example: username
     * @param text value for field*/
    public void checkEmpty(String field, String text, Locale locale) throws InputValidatorError
    {
        if(StringUtils.isEmpty(text))
        {
            String error = localeManager.getString("%s cannot be empty", locale, field);
            throw new InputValidatorError(error);
        }
    }

    /**
     * Check if email is valid*/
    public void checkEmailValid(String text, Locale locale) throws InputValidatorError
    {
        if(!JUtils.isEmailValid(text))
        {
            String error = localeManager.getString("E-Mail address is invalid", locale);
            throw new InputValidatorError(error);
        }
    }

    /**
     * Check length*/
    public void checkLength(String field, String text, Locale locale, int min, int max) throws InputValidatorError
    {
        if(StringUtils.isEmpty(text) || text.length() < min || text.length() > max)
        {
            String error = String.format(localeManager.getString("%s must be between %d and %d characters long.", locale), field, min, max);
            throw new InputValidatorError(error);
        }
    }

    /**
     * Check length*/
    public void checkLength(String field, String text, Locale locale, int min) throws InputValidatorError
    {
        if(StringUtils.isEmpty(text) || text.length() < min)
        {
            String error = String.format(localeManager.getString("%s must be longer than %d characters", locale), field, min);
            throw new InputValidatorError(error);
        }
    }
}