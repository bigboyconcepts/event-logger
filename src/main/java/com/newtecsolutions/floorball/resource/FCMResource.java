package com.newtecsolutions.floorball.resource;

import com.newtecsolutions.floorball.MyResponse;
import com.newtecsolutions.floorball.intercept.MyResourceInterceptor;
import com.newtecsolutions.floorball.intercept.Transactional;
import com.newtecsolutions.floorball.model.FCMRegistrationId;
import com.newtecsolutions.floorball.model.Member;
import com.newtecsolutions.floorball.model.OAuthAccessToken;
import com.newtecsolutions.floorball.model.Permission;
import com.newtecsolutions.floorball.param_converter.filter.FieldFilter;
import com.newtecsolutions.floorball.param_converter.sort.Sort;
import com.newtecsolutions.floorball.utils.HibernateUtil;

import org.hibernate.Session;

import javax.annotation.Nonnull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Created by pedja on 7/4/17 8:16 AM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */

@Path("/fcm")
@Api("fcm")
@Produces({MediaType.APPLICATION_JSON})
public class FCMResource extends BaseResource<FCMRegistrationId>
{
    @Override
    @Transactional(requiredPermissions = Permission.view_back)
    public Response get(long id)
    {
        return super.get(id);
    }

    @Override
    @Transactional(requiredPermissions = Permission.view_back)
    public Response count()
    {
        return super.count();
    }

    @Override
    @Transactional(requiredPermissions = Permission.view_back)
    public Response list(int page, int perPage, FieldFilter filter, Sort sort)
    {
        return super.list(page, perPage, filter, sort);
    }

    @Override
    @Transactional(requiredPermissions = Permission.view_back)
    public Response changeState(long id, Boolean active)
    {
        return super.changeState(id, active);
    }

    @Override
    public Response delete(long id)
    {
        return super.delete(id);
    }

    @POST
    @Path("/")
    @ApiOperation(value = "Register device for FCM messages")
    public Response register(@Nonnull @ApiParam(value = "FCM registration id", required = true) @QueryParam("registrationId") String registrationIdString,
                             @Nonnull @ApiParam(value = "Device id", required = true)@QueryParam("deviceId") String deviceId)
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Member member = ((OAuthAccessToken)request.getAttribute(MyResourceInterceptor.ATTR_TOKEN)).getMember();

        FCMRegistrationId registrationId = FCMRegistrationId.findRegistrationId(member, registrationIdString);
        if(registrationId != null)
        {
            registrationId.setFcmRegistrationId(registrationIdString);
        }
        else
        {
            registrationId = new FCMRegistrationId();
            registrationId.setFcmRegistrationId(registrationIdString);
            registrationId.setDeviceId(deviceId);
            registrationId.setMember(member);
        }

        session.save(registrationId);

        MyResponse response = new MyResponse();
        response.setData(registrationId);
        return response.toResponse();
    }

    @Nonnull
    @Override
    protected Class<FCMRegistrationId> getModelClass()
    {
        return FCMRegistrationId.class;
    }
}
