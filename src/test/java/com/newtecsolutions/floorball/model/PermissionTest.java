package com.newtecsolutions.floorball.model;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by pedja on 8/4/17 1:28 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class PermissionTest
{
    @Test
    public void testThatPermissionFromStringReturnsPermissionOrNull()
    {
        Assert.assertTrue(Permission.update_member == Permission.fromString("update_member"));
        Assert.assertTrue(Permission.delete_member == Permission.fromString("delete_member "));
        Assert.assertTrue(Permission.delete_club == Permission.fromString(" delete_club "));
        Assert.assertTrue(Permission.delete_club == Permission.fromString("delete_club"));
        Assert.assertNull(Permission.fromString("blbalbld"));
    }

    @Test
    public void testFromCsvList()
    {
        Assert.assertThat(Permission.listFromCsvString("update_member, delete_member"), Matchers.contains(Permission.update_member, Permission.delete_member));
        Assert.assertThat(Permission.listFromCsvString("update_member,delete_member"), Matchers.contains(Permission.update_member, Permission.delete_member));
        Assert.assertThat(Permission.listFromCsvString("update_member"), Matchers.contains(Permission.update_member));
    }

    @Test
    public void testFromList()
    {
        Assert.assertEquals(Permission.stringFromList(Arrays.asList(Permission.update_member, Permission.create_member)), "update_member,create_member");
    }
}
