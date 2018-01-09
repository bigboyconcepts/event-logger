package com.newtecsolutions.floorball.utils;

import org.junit.Assert;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by pedja on 8/2/17 12:52 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class PasswordUtilsTest
{
    @Test
    public void testPassword() throws InvalidKeySpecException, NoSuchAlgorithmException
    {
        String passwordPlain = "123456";
        String passwordHashed = PasswordUtils.generateStrongPasswordHash(passwordPlain);
        Assert.assertTrue(PasswordUtils.validatePassword(passwordPlain, passwordHashed));

        passwordPlain = "ms4R5jt.";
        passwordHashed = PasswordUtils.generateStrongPasswordHash(passwordPlain);
        Assert.assertTrue(PasswordUtils.validatePassword(passwordPlain, passwordHashed));
    }
}
