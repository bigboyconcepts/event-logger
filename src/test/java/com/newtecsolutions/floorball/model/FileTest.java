package com.newtecsolutions.floorball.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by pedja on 8/4/17 1:33 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class FileTest
{
    @Test
    public void testThatMimeTypeIsVideo()
    {
        for(String mimeType : File.VIDEO_EXTENSIONS)
        {
            Assert.assertTrue(File.isVideo("video/" + mimeType));
        }
    }

    @Test
    public void testThatMimeTypeIsImage()
    {
        for(String mimeType : File.IMAGE_EXTENSIONS)
        {
            Assert.assertTrue(File.isImage("image/" + mimeType));
        }
    }
}
