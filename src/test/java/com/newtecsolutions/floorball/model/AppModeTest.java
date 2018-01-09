package com.newtecsolutions.floorball.model;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;


/**
 * Created by pedja on 8/4/17 1:28 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class AppModeTest
{
    @Test
    public void testThatAppModeFromStringReturnsAppModeOrNull()
    {
        Assert.assertTrue(AppMode.club_basic == AppMode.fromString("club_basic"));
        Assert.assertTrue(AppMode.club_basic == AppMode.fromString("club_basic "));
        Assert.assertTrue(AppMode.club_basic == AppMode.fromString(" club_basic "));
        Assert.assertTrue(AppMode.home_trainer == AppMode.fromString("home_trainer"));
        Assert.assertTrue(AppMode.home_trainer == AppMode.fromString(" home_trainer"));
        Assert.assertTrue(AppMode.camp_training == AppMode.fromString("camp_training"));
        Assert.assertTrue(AppMode.camp_training == AppMode.fromString("camp_training "));
        Assert.assertNull(AppMode.fromString("blbalbld"));
    }

    @Test
    public void testFromCsvList()
    {
        Assert.assertThat(AppMode.listFromCsvString("club_basic, home_trainer"), Matchers.contains(AppMode.club_basic, AppMode.home_trainer));
        Assert.assertThat(AppMode.listFromCsvString("club_basic,home_trainer"), Matchers.contains(AppMode.club_basic, AppMode.home_trainer));
        Assert.assertThat(AppMode.listFromCsvString("club_basic"), Matchers.contains(AppMode.club_basic));
    }

    @Test
    public void testFromList()
    {
        Assert.assertEquals(AppMode.stringFromList(Arrays.asList(AppMode.home_trainer, AppMode.club_basic)), "home_trainer,club_basic");
    }
}
