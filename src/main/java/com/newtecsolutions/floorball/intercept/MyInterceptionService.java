package com.newtecsolutions.floorball.intercept;

import com.newtecsolutions.floorball.utils.AnnotationUtils;
import com.newtecsolutions.floorball.utils.LocaleManager;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.glassfish.hk2.api.Filter;
import org.jvnet.hk2.annotations.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/**
 * @author Michal Gajdos
 */
@Service
public class MyInterceptionService implements org.glassfish.hk2.api.InterceptionService
{
    private LocaleManager localeManager;

    @Inject
    MyInterceptionService(LocaleManager localeManager)
    {
        this.localeManager = localeManager;
        TRANSACTIONAL_RESOURCE_METHOD_INTERCEPTORS = Collections.singletonList(new MyResourceInterceptor(localeManager));
    }

    private final List<MethodInterceptor> TRANSACTIONAL_RESOURCE_METHOD_INTERCEPTORS;

    @Override
    public Filter getDescriptorFilter()
    {
        //intercept only methods from our package
        return d ->
        {
            final String clazz = d.getImplementation();
            return clazz.startsWith("com.newtecsolutions.floorball");
        };
    }

    @Override
    public List<MethodInterceptor> getMethodInterceptors(final Method method)
    {
        // Apply interceptors only to methods annotated with @Transactional.
        Transactional transactional = AnnotationUtils.getInheritedAnnotation(Transactional.class, method);
        if (transactional != null)
        {
            return TRANSACTIONAL_RESOURCE_METHOD_INTERCEPTORS;
        }
        return null;
    }

    @Override
    public List<ConstructorInterceptor> getConstructorInterceptors(final Constructor<?> constructor)
    {
        return null;
    }
}