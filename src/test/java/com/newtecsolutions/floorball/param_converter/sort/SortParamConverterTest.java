package com.newtecsolutions.floorball.param_converter.sort;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by pedja on 8/2/17 2:05 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class SortParamConverterTest
{
    private SortParamConverter converter;

    @Before
    public void before()
    {
        converter = new SortParamConverter();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromString()
    {
        Sort sort = converter.fromString("{\n" +
                "\t\"field\": \"name\",\n" +
                "\t\"order\": \"DESC\"\n" +
                "}");
        Assert.assertNotNull(sort);
        Assert.assertEquals("name", sort.getField());
        Assert.assertEquals(Sort.Order.DESC, sort.getOrder());

        sort = converter.fromString(null);
        Assert.assertNull(sort);

        sort = converter.fromString("");
        Assert.assertNull(sort);
    }
}
