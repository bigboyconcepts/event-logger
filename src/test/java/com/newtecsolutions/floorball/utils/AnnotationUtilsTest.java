package com.newtecsolutions.floorball.utils;

import com.newtecsolutions.floorball.intercept.Transactional;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by pedja on 8/2/17 1:33 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class AnnotationUtilsTest
{
    @Test
    public void testGetInheritedAnnotation() throws NoSuchMethodException
    {
        Transactional transactional = AnnotationUtils.getInheritedAnnotation(Transactional.class, AnnotatedClass1Subclass1.class.getMethod("annotatedMethodDirect"));
        Assert.assertNotNull(transactional);

        transactional = AnnotationUtils.getInheritedAnnotation(Transactional.class, AnnotatedClass1Subclass1.class.getMethod("annotatedMethodOverride"));
        Assert.assertNotNull(transactional);

        transactional = AnnotationUtils.getInheritedAnnotation(Transactional.class, AnnotatedClass1Subclass1.class.getMethod("notAnnotatedMethod"));
        Assert.assertNull(transactional);

        transactional = AnnotationUtils.getInheritedAnnotation(Transactional.class, AnnotatedClass2Subclass1.class.getMethod("annotatedMethodOverride", String.class, long.class));
        Assert.assertNotNull(transactional);
    }

    private class AnnotatedClass1
    {
        @Transactional
        public void annotatedMethodOverride()
        {

        }
    }

    private class AnnotatedClass1Subclass1 extends AnnotatedClass1
    {
        @Transactional
        public void annotatedMethodDirect()
        {

        }


        @Override
        public void annotatedMethodOverride()
        {

        }

        public void notAnnotatedMethod()
        {

        }
    }

    private class AnnotatedClass2<T>
    {
        @Transactional
        public void annotatedMethodOverride(T object, long id)
        {

        }
    }

    private class AnnotatedClass2Subclass1 extends AnnotatedClass2<String>
    {
        @Transactional
        public void annotatedMethodDirect()
        {

        }

        @Override
        public void annotatedMethodOverride(String object, long id)
        {

        }
    }
}
