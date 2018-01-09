package com.newtecsolutions.floorball.param_converter.filter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

/**
 * Created by pedja on 6/26/17 3:04 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
@Provider
public class FieldFilterParamConverterProvider implements ParamConverterProvider
{
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations)
    {
        return rawType == FieldFilter.class ? (ParamConverter<T>) new FieldFilterParamConverter() : null;
    }
}
