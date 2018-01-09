package com.newtecsolutions.floorball.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by pedja on 6/22/17 10:05 AM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class AnnotationUtils
{
    private AnnotationUtils()
    {
    }

    /**
     * <pre>
     * Try to get annotation from class methods, also searching in superclasses
     * First it tries default {@link AnnotatedElement#getAnnotation(Class)}, if it fails it will search in superclasses
     * </pre>*/
    public static <A extends Annotation> A getInheritedAnnotation(Class<A> annotationClass, AnnotatedElement element)
    {
        A annotation = element.getAnnotation(annotationClass);
        if (annotation == null && element instanceof Method)
            annotation = getOverriddenAnnotation(annotationClass, (Method) element);
        return annotation;
    }

    /**
     * Search for annotation in superclasses*/
    private static <A extends Annotation> A getOverriddenAnnotation(Class<A> annotationClass, Method method)
    {
        final Class<?> methodClass = method.getDeclaringClass();
        final String name = method.getName();
        final Class<?>[] params = method.getParameterTypes();

        // prioritize all superclasses over all interfaces
        final Class<?> superclass = methodClass.getSuperclass();
        if (superclass != null)
        {
            //get generic superclass of this class, so we can determine generic type
            Type genericType = methodClass.getGenericSuperclass();

            if (genericType instanceof ParameterizedType)
            {
                ParameterizedType genericSuperclass = (ParameterizedType) genericType;

                //get generic type
                Type[] genericTypes = genericSuperclass.getActualTypeArguments();
                if (genericTypes.length > 0)
                {
                    Class<?> typeClass = (Class<?>) genericSuperclass.getActualTypeArguments()[0];
                    //replace generic type (T) with Object.class so that we can find it in superclass
                    replaceConcreteParameterTypeWithObject(params, typeClass);
                }
            }

            final A annotation = getOverriddenAnnotationFrom(annotationClass, superclass, name, params);
            if (annotation != null)
                return annotation;
        }

        // depth-first search over interface hierarchy
        for (final Class<?> intf : methodClass.getInterfaces())
        {
            final A annotation = getOverriddenAnnotationFrom(annotationClass, intf, name, params);
            if (annotation != null)
                return annotation;
        }

        return null;
    }

    private static void replaceConcreteParameterTypeWithObject(Class<?>[] params, Class<?> typeClass)
    {
        for (int i = 0; i < params.length; i++)
        {
            Class<?> type = params[i];
            if (typeClass == type)
            {
                params[i] = Object.class;
            }
        }
    }

    private static <A extends Annotation> A getOverriddenAnnotationFrom(Class<A> annotationClass, Class<?> searchClass, String name, Class<?>[] params)
    {
        try
        {
            final Method method = searchClass.getMethod(name, params);
            final A annotation = method.getAnnotation(annotationClass);
            if (annotation != null)
                return annotation;
            return getOverriddenAnnotation(annotationClass, method);
        }
        catch (final NoSuchMethodException e)
        {
            return null;
        }
    }
}
