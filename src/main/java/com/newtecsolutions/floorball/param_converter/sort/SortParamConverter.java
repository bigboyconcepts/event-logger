package com.newtecsolutions.floorball.param_converter.sort;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.skynetsoftware.jutils.StringUtils;

import java.io.IOException;

import javax.ws.rs.ext.ParamConverter;

/**
 * Created by pedja on 6/26/17 3:06 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class SortParamConverter implements ParamConverter<Sort>
{
    @Override
    public Sort fromString(String json)
    {
        ObjectMapper mapper = new ObjectMapper();
        assertEmptyJson(json);
        try
        {
            Sort sort = mapper.readValue(json, Sort.class);
            if(StringUtils.isEmpty(sort.getField()))
                return null;
            return sort;
        }
        catch (IOException e)
        {
            return null;
        }
    }

    @Override
    public String toString(Sort value)
    {
        //TODO does nothing
        assertEmptyFilter(value);
        return value.toString();
    }

    private void assertEmptyFilter(Sort value)
    {
        if(value == null)
            throw new IllegalArgumentException("sort cannot be null");
    }

    private void assertEmptyJson(String json)
    {
        if(StringUtils.isEmpty(json))
            throw new IllegalArgumentException("sort cannot be null");
    }
}
