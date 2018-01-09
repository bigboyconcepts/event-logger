package com.newtecsolutions.floorball.param_converter.filter;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by pedja on 8/4/17 12:56 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class FieldFilterTest
{
    @Test
    public void testQueryWherePart()
    {
        FieldFilter fieldFilter = new FieldFilter("{\n" +
                "  \"name\": \"pera\",\n" +
                "\t\"city\": \"Beograd\",\n" +
                "  \"id\": [\n" +
                "  \t1, 2, 3\n" +
                "  ]\n" +
                "}");
        Assert.assertEquals(" name=:_name and city=:_city and id in (:_id)", fieldFilter.createQueryWherePart(null));
        Assert.assertEquals(" mmbr.name=:_name and mmbr.city=:_city and mmbr.id in (:_id)", fieldFilter.createQueryWherePart("mmbr."));
    }
}
