package com.newtecsolutions.floorball.resource;

import com.newtecsolutions.floorball.MyResponse;
import com.newtecsolutions.floorball.intercept.Transactional;
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

@Path("/permission")
@Api("permission")
@Produces({MediaType.APPLICATION_JSON})
public class PermissionResource
{
    @Context
    HttpServletRequest request;

    @GET
    @Path("/")
    @ApiOperation(value = "Get all permissions",
            response = Permission.class)
    @Transactional(requiresAuth = false)
    public Response all()
    {
        ActivityManager.getInstance().add(request, null, "GET /permissions");

        MyResponse response = new MyResponse();
        response.setData(Permission.values());
        return response.toResponse();
    }

    public HttpServletRequest getRequest()
    {
        return request;
    }
}