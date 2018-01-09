package com.newtecsolutions.floorball;

import org.opencv.core.Core;

/**
 * Wrapper for loading native library for opencv, not really needed....*/
public class OpenCVJNI
{
    static
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void init(){}//does nothing used for clinit
}
