package com.newtecsolutions.floorball.param_converter.app_mode;

import com.newtecsolutions.floorball.model.AppMode;

import org.skynetsoftware.jutils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedja on 6/29/17 3:26 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class AppModeParam
{
    private final List<AppMode> appModes;

    public AppModeParam(String value)
    {
        appModes = new ArrayList<>();
        if(!StringUtils.isEmpty(value))
        {
            String[] split = value.split(",\\s*");
            for(String mode : split)
            {
                AppMode appMode = AppMode.fromString(mode);
                if(appMode != null)appModes.add(appMode);
            }
        }
    }

    public List<AppMode> getAppModes()
    {
        return appModes;
    }
}
