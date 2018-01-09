package com.newtecsolutions.floorball.resource;

import com.newtecsolutions.floorball.FBException;
import com.newtecsolutions.floorball.MyResponse;
import com.newtecsolutions.floorball.Paginator;
import com.newtecsolutions.floorball.fcm.FcmHandler;
import com.newtecsolutions.floorball.intercept.Transactional;
import com.newtecsolutions.floorball.model.FCMRegistrationId;
import com.newtecsolutions.floorball.model.File;
import com.newtecsolutions.floorball.model.Notification;
import com.newtecsolutions.floorball.model.Permission;
import com.newtecsolutions.floorball.param_converter.filter.FieldFilter;
import com.newtecsolutions.floorball.param_converter.sort.Sort;
import com.newtecsolutions.floorball.utils.ActivityManager;
import com.newtecsolutions.floorball.utils.HibernateUtil;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

@Path("/notification")
@Api("notification")
@Produces({MediaType.APPLICATION_JSON})
public class NotificationResource extends BaseResource<Notification>
{
    @Override
    public Response list(int page, int perPage, FieldFilter filter, Sort sort)
    {
        Paginator<Notification> paginator = new Paginator<>(getModelClass());

        Query query;
        if (filter != null)
        {
            query = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from " + getModelClass().getSimpleName() + " notif left join fetch notif.image where " + filter.createQueryWherePart("notif.") + (sort != null ? (sort.createSortQueryPart(getModelClass())) : ""));
            filter.bindParams(query, getModelClass());
        }
        else if (sort != null)
        {
            query = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from " + getModelClass().getSimpleName() + " notif left join fetch notif.image " + sort.createSortQueryPart(getModelClass()));
        }
        else
        {
            query = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from " + getModelClass().getSimpleName() + " notif left join fetch notif.image");
        }

        Paginator.Page<Notification> _page = paginator.nextPage(page, perPage, query);

        ActivityManager.getInstance().add(request, null, "GET /" + getModelClass().getSimpleName().toLowerCase());

        MyResponse response = new MyResponse();
        response.setData(_page);
        return response.toResponse();
    }

    @Override
    @Transactional(requiredPermissions = Permission.view_back)
    public Response changeState(long id, Boolean active)
    {
        return super.changeState(id, active);
    }

    @Override
    @Transactional(requiredPermissions = Permission.view_back)
    public Response delete(long id)
    {
        return super.delete(id);
    }

    @POST
    @Path("/")
    @ApiOperation(value = "Create notification and send it via fcm")
    @Transactional(requiredPermissions = Permission.view_back)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response create(@Nonnull @ApiParam(value = "Title", required = true) @FormDataParam("title") String title,
                           @Nonnull @ApiParam(value = "Text", required = true)@FormDataParam("text") String text,
                           @ApiParam(value = "Notification photo", required = false) @FormDataParam("image") InputStream imageInputStream,
                           @FormDataParam("image") FormDataContentDisposition imageMetaData)
    {
        if (imageInputStream != null && !File.isImage(imageMetaData))
            throw new FBException(MyResponse.ErrorCode.api_error, 400, localeManager.getString("Unsupported file for 'image' param", request.getLocale()));

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Notification notification = new Notification();
        notification.setText(text);
        notification.setTitle(title);
        session.save(notification);

        if (imageInputStream != null)
        {
            File file = File.uploadFile(notification, imageInputStream, imageMetaData, localeManager);
            notification.setImage(file);
        }
        session.save(notification);

        FcmHandler.getInstance().send(notification, FCMRegistrationId.getRegistrationIdsAsStringList());

        MyResponse response = new MyResponse();
        response.setData(notification);
        return response.toResponse();
    }

    @Nonnull
    @Override
    protected Class<Notification> getModelClass()
    {
        return Notification.class;
    }
}
