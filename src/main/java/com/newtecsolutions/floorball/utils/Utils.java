package com.newtecsolutions.floorball.utils;


import org.skynetsoftware.jutils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by pedja on 23.9.16. 09.56.
 * This class is part of the Floorball
 * Copyright © 2016 ${OWNER}
 */
public class Utils
{
    private Utils()
    {
    }

    /**
     * Create CSV String from List<String>*/
    public static String serializeStringList(List<String> list)
    {
        if(list == null || list.isEmpty())
            return null;
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < list.size(); i++)
        {
            if(i != 0)
                builder.append(",");
            builder.append(list.get(i));
        }
        return builder.toString();
    }

    /**
     * Create List<String> from CSV string*/
    public static List<String> deserializeStringList(String string)
    {
        if(StringUtils.isEmpty(string))
            return Collections.emptyList();
        List<String> list = new ArrayList<>();
        String[] split = string.split("\\s*,\\s*");
        Collections.addAll(list, split);
        return list;
    }
}
