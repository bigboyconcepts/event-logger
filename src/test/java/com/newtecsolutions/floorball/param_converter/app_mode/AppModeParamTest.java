package com.newtecsolutions.floorball.param_converter.app_mode;

import com.newtecsolutions.floorball.model.AppMode;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by pedja on 8/4/17 1:12 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class AppModeParamTest
{
    @Test
    public void testAppModeParamsConvertsFromString()
    {
        AppModeParam param = new AppModeParam("club_basic, home_trainer, camp_training");
        Assert.assertThat(param.getAppModes(), Matchers.contains(AppMode.club_basic, AppMode.home_trainer, AppMode.camp_training));

        param = new AppModeParam("club_basic, home_trainer,            camp_training");
        Assert.assertThat(param.getAppModes(), Matchers.contains(AppMode.club_basic, AppMode.home_trainer, AppMode.camp_training));

        param = new AppModeParam("club_basic, home_trainer,camp_training");
        Assert.assertThat(param.getAppModes(), Matchers.contains(AppMode.club_basic, AppMode.home_trainer, AppMode.camp_training));

        param = new AppModeParam("club_basic, home_trainer,vbsss");
        Assert.assertThat(param.getAppModes(), Matchers.contains(AppMode.club_basic, AppMode.home_trainer));
    }
}
