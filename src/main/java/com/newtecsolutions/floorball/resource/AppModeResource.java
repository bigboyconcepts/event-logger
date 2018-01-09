package com.newtecsolutions.floorball.resource;

import com.newtecsolutions.floorball.MyResponse;
import com.newtecsolutions.floorball.intercept.Transactional;
import com.newtecsolutions.floorball.model.AppMode;
import com.newtecsolutions.floorball.model.Permission;
import com.newtecsolutions.floorball.utils.ActivityManager;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/appmode")
@Api("appmode")
@Produces({MediaType.APPLICATION_JSON})
public class AppModeResource
{
    @Context
    HttpServletRequest request;

    @GET
    @Path("/")
    @ApiOperation(value = "Get list of app modes",
            response = Permission.class)
    @Transactional(requiresAuth = false)
    public Response all()
    {
        ActivityManager.getInstance().add(request, null, "GET /appmodes");

        MyResponse response = new MyResponse();
        response.setData(AppMode.values());
        return response.toResponse();
    }

    public HttpServletRequest getRequest()
    {
        return request;
    }
}