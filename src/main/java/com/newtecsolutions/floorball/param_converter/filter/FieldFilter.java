package com.newtecsolutions.floorball.param_converter.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newtecsolutions.floorball.utils.HibernateUtil;

import org.skynetsoftware.jutils.StringUtils;

import java.util.List;
import java.util.Map;

import javax.persistence.Query;

/**
 * Created by pedja on 6/26/17 3:07 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class FieldFilter
{
    private Map<String, Object> fields;
    private ObjectMapper mapper = new ObjectMapper();

    public FieldFilter(String json)
    {
        try
        {
            fields = mapper.readValue(json, new TypeReference<Map<String, Object>>(){});
        }
        catch (Exception e)
        {
            throw new IllegalStateException("filter is not valid");
        }
    }

    @Override
    public String toString()
    {
        try
        {
            return mapper.writeValueAsString(fields);
        }
        catch (JsonProcessingException e)
        {
            throw new IllegalStateException("filter is not valid");
        }
    }

    /**
     * Create 'where' part of the query excluding 'where'.
     * @return example: 'username = pedja AND password = 123456'*/
    public String createQueryWherePart(String pathPrefix)
    {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for(String key : fields.keySet())
        {
            Object value = fields.get(key);
            if(index != 0)
                builder.append(" and");
            //if its a list us 'where in'
            if(value instanceof List)
            {
                builder.append(" ").append(!StringUtils.isEmpty(pathPrefix) ? pathPrefix : "").append(key).append(" in (:_").append(key.replaceAll("\\.", "_")).append(")");
            }
            else
            {
                builder.append(" ").append(!StringUtils.isEmpty(pathPrefix) ? pathPrefix : "").append(key).append("=:_").append(key.replaceAll("\\.", "_"));
            }
            index++;
        }
        return builder.toString();
    }

    /**
     * Bind parameters to query*/
    public void bindParams(Query query, Class<?> modelClass)
    {
        for(String key : fields.keySet())
        {
            //this is hack for int to long
            Class<?> type = getAttributeType(modelClass, key);
            Object value = fields.get(key);
            if(value instanceof List)
            {
                List objects = (List) value;
                for(int i = 0; i < objects.size(); i++)
                {
                    Object object = objects.get(i);
                    //because jackson assumes that numbers are int, and field is long, convert it to long if int
                    if (type == long.class && !(object instanceof Long) && object instanceof Integer)
                    {
                        object = ((Integer)object).longValue();
                        objects.set(i, object);
                    }
                }
            }
            else
            {
                //because jackson assumes that numbers are int, and field is long, convert it to long if int
                if (type == long.class && !(value instanceof Long) && value instanceof Integer)
                {
                    value = ((Integer)value).longValue();
                }
            }
            query.setParameter("_" + key.replaceAll("\\.", "_"), value);
        }
    }

    /**
     * <pre>
     * Get class of field.
     * It also supports nested fileds, for example in member.address.city it will get type of city in Address model
     * </pre>*/
    private Class<?> getAttributeType(Class<?> modelClass, String key)
    {
        if(key.contains("."))
        {
            String first = key.split("\\.")[0];
            String rest = key.substring(key.indexOf('.') + 1, key.length());
            Class<?> clazz = HibernateUtil.getSessionFactory().getMetamodel().entity(modelClass).getAttribute(first).getJavaType();
            return getAttributeType(clazz, rest);
        }
        else
        {
            return HibernateUtil.getSessionFactory().getMetamodel().entity(modelClass).getAttribute(key).getJavaType();
        }

    }
}
