package com.newtecsolutions.floorball.param_converter.filter;

import org.skynetsoftware.jutils.StringUtils;

import javax.ws.rs.ext.ParamConverter;

/**
 * Created by pedja on 6/26/17 3:06 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class FieldFilterParamConverter implements ParamConverter<FieldFilter>
{
    @Override
    public FieldFilter fromString(String json)
    {
        assertEmptyJson(json);
        return new FieldFilter(json);
    }

    @Override
    public String toString(FieldFilter value)
    {
        assertEmptyFilter(value);
        return value.toString();
    }

    private void assertEmptyFilter(FieldFilter value)
    {
        if(value == null)
            throw new IllegalArgumentException("filter cannot be null");
    }

    private void assertEmptyJson(String json)
    {
        if(StringUtils.isEmpty(json))
            throw new IllegalArgumentException("filter cannot be null");
    }
}
