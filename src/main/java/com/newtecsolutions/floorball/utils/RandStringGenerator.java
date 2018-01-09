package com.newtecsolutions.floorball.utils;


import org.skynetsoftware.jutils.RandomString;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pedja on 19.9.16. 14.09.
 * This class is part of the Floorball
 * Copyright © 2016 ${OWNER}
 */

public class RandStringGenerator
{
    private static final Map<Integer, RandomString> GENERATORS = new HashMap<>();

    /**
     * Generate random string with length*/
    public static synchronized String nextString(int length)
    {
        RandomString generator = GENERATORS.computeIfAbsent(length, RandomString::new);
        return generator.nextString();
    }
}
