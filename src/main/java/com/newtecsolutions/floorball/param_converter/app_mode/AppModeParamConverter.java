package com.newtecsolutions.floorball.param_converter.app_mode;

import javax.ws.rs.ext.ParamConverter;

/**
 * Created by pedja on 6/26/17 3:06 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class AppModeParamConverter implements ParamConverter<AppModeParam>
{
    @Override
    public AppModeParam fromString(String value)
    {
        return new AppModeParam(value);
    }

    @Override
    public String toString(AppModeParam value)
    {
        return null;
    }
}
