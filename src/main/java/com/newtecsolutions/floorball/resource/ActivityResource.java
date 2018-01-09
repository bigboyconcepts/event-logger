package com.newtecsolutions.floorball.resource;

import com.newtecsolutions.floorball.FBException;
import com.newtecsolutions.floorball.MyResponse;
import com.newtecsolutions.floorball.utils.ActivityManager;
import com.newtecsolutions.floorball.utils.LocaleManager;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Created by pedja on 6/22/17 11:56 AM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
@Path("/activity")
@Api("activity")
@Produces({MediaType.APPLICATION_JSON})
public class ActivityResource
{
    @Context
    HttpServletRequest request;

    private LocaleManager localeManager;

    @Inject
    public ActivityResource(LocaleManager localeManager)
    {
        this.localeManager = localeManager;
    }

    @GET
    @Path("/")
    @ApiOperation(value = "Get members activities")
    public Response activity(@ApiParam("token") @QueryParam("token") String token)
    {
        if (!ActivityManager.TOKEN.equals(token))
        {
            throw new FBException(MyResponse.ErrorCode.api_error, localeManager.getString("Invalid token: " + token, request.getLocale()));
        }
        MyResponse response = new MyResponse();
        List<ActivityManager.Activity> activities = ActivityManager.getInstance().getActivities();
        Collections.sort(activities);
        response.setData(activities);

        return response.toResponse();
    }
}
