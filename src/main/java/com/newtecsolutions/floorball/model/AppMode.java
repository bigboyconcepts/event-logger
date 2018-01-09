package com.newtecsolutions.floorball.model;

import org.skynetsoftware.jutils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedja on 6/29/17 8:11 AM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public enum AppMode
{
    club_basic, home_trainer, camp_training;

    public static AppMode fromString(String appMode)
    {
        if(StringUtils.isEmpty(appMode))
            return null;
        for(AppMode mode : values())
        {
            if(mode.toString().equals(appMode.trim()))
                return mode;
        }
        return null;
    }

    public static List<AppMode> listFromCsvString(String appModes)
    {
        List<AppMode> modesList = new ArrayList<>();
        if(StringUtils.isEmpty(appModes))
            return modesList;
        String[] modesSplit = appModes.split("\\s*,\\s*");
        for(String appMode : modesSplit)
        {
            AppMode mode = AppMode.fromString(appMode);
            if(appMode != null)
                modesList.add(mode);
        }
        return modesList;
    }

    public static String stringFromList(List<AppMode> appModes)
    {
        StringBuilder builder = new StringBuilder();
        for(int i = 0 ; i < appModes.size() ; i++)
        {
            AppMode appMode = appModes.get(i);
            if(i != 0)
                builder.append(",");
            builder.append(appMode.toString());
        }
        return builder.toString();
    }
}
