package com.newtecsolutions.floorball.utils;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by pedja on 16.9.16. 13.15.
 * This class is part of the Floorball
 * Copyright © 2016 ${OWNER}
 */

public class LocaleManager
{
    /**
     * <pre>
     * Return localized string for text
     * If translation is not found for passed locale, passed text is returned
     * @param text text to translate.
     * @param locale locale to translate to
     * @param args optional String.format arguments
     * </pre>*/
    public String getString(String text, Locale locale, Object... args)
    {
        if(text == null)
            return null;
        if(locale == null)
            locale = Locale.US;
        try
        {
            ResourceBundle strings = ResourceBundle.getBundle("Strings", locale);
            if(args != null)
                return String.format(locale, strings.getString(text), args);
            return strings.getString(text);
        }
        catch (Exception e)
        {
            if(args != null)
                return String.format(locale, text, args);
            return text;
        }
    }
}
