package com.newtecsolutions.floorball;

import com.newtecsolutions.floorball.di.DIBinder;
import com.newtecsolutions.floorball.intercept.MyInterceptionBinder;
import com.newtecsolutions.floorball.utils.ObjectMapperContextResolver;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;

public class MyApplication extends ResourceConfig
{
    public MyApplication()
    {
        super();
        //register all providers, services, resources
        packages("io.swagger.jaxrs.listing", "com.newtecsolutions.floorball");
        register(FreemarkerMvcFeature.class);
        register(LoggingFeature.class);
        register(ObjectMapperContextResolver.class);
        register(new MultiPartFeature());
        register(new MyInterceptionBinder());
        register(new DIBinder());

        registerClasses(EncodingFilter.class, GZipEncoder.class, DeflateEncoder.class);
    }
}