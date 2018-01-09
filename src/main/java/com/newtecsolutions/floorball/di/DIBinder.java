package com.newtecsolutions.floorball.di;

import com.newtecsolutions.floorball.input_validator.SimpleInputValidator;
import com.newtecsolutions.floorball.utils.LocaleManager;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

/**
 * Created by pedja on 8/3/17 8:06 AM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class DIBinder extends AbstractBinder
{
    @Override
    protected void configure()
    {
        bind(LocaleManager.class).to(LocaleManager.class).in(Singleton.class);
        bind(SimpleInputValidator.class).to(SimpleInputValidator.class).in(Singleton.class);
    }
}
