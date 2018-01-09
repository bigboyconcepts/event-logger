package com.newtecsolutions.floorball.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper>
{
    private final ObjectMapper mapper;

    public ObjectMapperContextResolver()
    {
        //this is used to prevent jackson to try to serialize lazy (not loaded) hibernate objects, which would cause
        //exception since at the point of serialization hibernate transaction is already closed
        mapper = new ObjectMapper();
        mapper.registerModule(new Hibernate5Module());
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}