package com.newtecsolutions.floorball.intercept;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

/**
 * Register our custom {@code InterceptionService} into HK2.
 *
 * @author Michal Gajdos
 */
public class MyInterceptionBinder extends AbstractBinder
{
    @Override
    protected void configure()
    {
        bind(MyInterceptionService.class)
                .to(org.glassfish.hk2.api.InterceptionService.class)
                .in(Singleton.class);
    }
}