package com.newtecsolutions.floorball.model;

import org.hamcrest.number.OrderingComparison;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by pedja on 8/4/17 2:03 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class RegistrationCodeTest
{
    @Test
    public void testThatRegistrationCodeIsInRange()
    {
        for(int i = 0; i < 10000; i++)
        {
            int random = RegistrationCode.newRandomCode();
            Assert.assertThat(random, OrderingComparison.lessThanOrEqualTo(999999));
            Assert.assertThat(random, OrderingComparison.greaterThanOrEqualTo(0));
        }
    }
}
