package com.newtecsolutions.floorball.utils;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by pedja on 8/2/17 11:03 AM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */

public class UtilsTest
{
    @Test
    public void testSerializeListToString()
    {
        List<String> test1 = Arrays.asList("1", "2", "3", "4");
        Assert.assertEquals("1,2,3,4", Utils.serializeStringList(test1));
    }

    @Test
    public void testDeserializeStringToList()
    {
        String test1 = "1,2,3,4";
        String test2 = "1, 2,3, 4,";
        String test3 = "1 , 2,3, 4,";

        String[] items = {"1", "2", "3", "4"};

        Assert.assertThat(Utils.deserializeStringList(test1), Matchers.contains(items));
        Assert.assertThat(Utils.deserializeStringList(test2), Matchers.contains(items));
        Assert.assertThat(Utils.deserializeStringList(test3), Matchers.contains(items));
    }
}
