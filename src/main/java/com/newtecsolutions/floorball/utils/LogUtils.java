package com.newtecsolutions.floorball.utils;

import org.skynetsoftware.jutils.StringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pedja on 16.9.16. 09.11.
 * This class is part of the Floorball
 * Copyright © 2016 ${OWNER}
 */

public class LogUtils
{
    private final static Logger LOGGER = Logger.getLogger(LogUtils.class.getName());

    static
    {
        String logLevelString = ConfigManager.getInstance().getString(ConfigManager.CONFIG_LOG_LEVEL_NAME, "INFO");
        Level logLevel = Level.INFO;
        if(!StringUtils.isEmpty(logLevelString))
        {
            switch (logLevelString)
            {
                case "OFF":
                    logLevel = Level.OFF;
                    break;
                case "SEVERE":
                    logLevel = Level.SEVERE;
                    break;
                case "WARNING":
                    logLevel = Level.WARNING;
                    break;
                case "INFO":
                    logLevel = Level.INFO;
                    break;
                case "CONFIG":
                    logLevel = Level.CONFIG;
                    break;
                case "FINE":
                    logLevel = Level.FINE;
                    break;
                case "FINER":
                    logLevel = Level.FINER;
                    break;
                case "FINEST":
                    logLevel = Level.FINEST;
                    break;
                case "ALL":
                    logLevel = Level.ALL;
                    break;
                default:
                    invalidConfigWarning(logLevelString);
                    break;
            }
        }
        else
        {
            invalidConfigWarning(logLevelString);
        }
        LOGGER.setLevel(logLevel);
    }

    private static void invalidConfigWarning(String logLevelString)
    {
        LOGGER.setLevel(Level.WARNING);
        LOGGER.warning(String.format("Invalid value %s in config for %s, using: %s", logLevelString, ConfigManager.CONFIG_LOG_LEVEL_NAME, Level.INFO.getName()));
    }

    public static void finest(String s)
    {
        LOGGER.finest(s);
    }

    public static void finer(String s)
    {
        LOGGER.finer(s);
    }

    public static void fine(String s)
    {
        LOGGER.fine(s);
    }

    public static void config(String s)
    {
        LOGGER.config(s);
    }

    public static void info(String s)
    {
        LOGGER.info(s);
    }

    public static void severe(String s)
    {
        LOGGER.severe(s);
    }

    public static void warning(String s)
    {
        LOGGER.warning(s);
    }

    public static Logger getLogger()
    {
        return LOGGER;
    }
}
