package com.newtecsolutions.floorball.model;

import com.newtecsolutions.floorball.FBException;
import com.newtecsolutions.floorball.MyResponse;
import com.newtecsolutions.floorball.utils.LocaleManager;

import org.skynetsoftware.jutils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by pedja on 6/20/17 2:36 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public enum Permission
{
    view_front, view_back, can_register,
    delete_member, create_member, update_member,
    delete_track, create_track, update_track,
    delete_role, create_role, update_role,
    delete_club, create_club, update_club,
    create_session,
    appmode_club_basic, appmode_home_trainer, appmode_camp_training,
    trainer, parent, student,
    ;

    public static Permission fromString(String permission)
    {
        if(StringUtils.isEmpty(permission))
            return null;
        for(Permission perm : values())
        {
            if(perm.toString().equals(permission.trim()))
                return perm;
        }
        return null;
    }

    public static List<Permission> listFromCsvString(String permissions)
    {
        List<Permission> permissionsList = new ArrayList<>();
        if(StringUtils.isEmpty(permissions))
            return permissionsList;
        String[] permsSplit = permissions.split("\\s*,\\s*");
        for(String perm : permsSplit)
        {
            Permission permission = Permission.fromString(perm);
            if(perm != null)
                permissionsList.add(permission);
        }
        return permissionsList;
    }

    public static String stringFromList(List<Permission> permissions)
    {
        StringBuilder builder = new StringBuilder();
        for(int i = 0 ; i < permissions.size() ; i++)
        {
            Permission permission = permissions.get(i);
            if(i != 0)
                builder.append(",");
            builder.append(permission.toString());
        }
        return builder.toString();
    }

    /**
     * Returns generic exception that member doesn't have permission to access some resource*/
    public static FBException getNoPermissionException(HttpServletRequest request, LocaleManager localeManager)
    {
        return new FBException(MyResponse.ErrorCode.permission_error, localeManager.getString("You don't have permission to access this resource", request == null ? null : request.getLocale()));
    }
}
