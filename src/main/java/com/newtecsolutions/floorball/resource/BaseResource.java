package com.newtecsolutions.floorball.resource;

import com.newtecsolutions.floorball.FBException;
import com.newtecsolutions.floorball.MyResponse;
import com.newtecsolutions.floorball.Paginator;
import com.newtecsolutions.floorball.param_converter.filter.FieldFilter;
import com.newtecsolutions.floorball.intercept.Transactional;
import com.newtecsolutions.floorball.model.HibernateModel;
import com.newtecsolutions.floorball.param_converter.sort.Sort;
import com.newtecsolutions.floorball.utils.ActivityManager;
import com.newtecsolutions.floorball.utils.HibernateUtil;
import com.newtecsolutions.floorball.utils.LocaleManager;

import org.hibernate.Session;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.PATCH;

/**
 * Created by pedja on 6/21/17 4:27 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public abstract class BaseResource<T>
{
    @Context
    HttpServletRequest request;

    @Inject
    protected LocaleManager localeManager;

    @GET
    @Path("/{id}")
    @ApiOperation(value = "Get object by id")
    @Transactional
    public Response get(@ApiParam(value = "id", required = true) @PathParam("id") long id)
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        T object = session.get(getModelClass(), id);

        ActivityManager.getInstance().add(request, null, "GET /" + getModelClass().getSimpleName().toLowerCase() + "/{id}");

        if (object == null)
        {
            return MyResponse.errorResponse(MyResponse.ErrorCode.not_found, getNotFoundErrorKey());
        }

        MyResponse response = new MyResponse();
        response.setData(object);
        return response.toResponse();
    }

    @GET
    @Path("/count")
    @ApiOperation(value = "Get object count")
    @Transactional
    public Response count()
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        Long count = (Long) session.createQuery("select count(1) from " + getModelClass().getSimpleName()).getSingleResult();

        ActivityManager.getInstance().add(request, null, "GET /" + getModelClass().getSimpleName().toLowerCase() + "/count");

        MyResponse response = new MyResponse();
        response.setData(count);
        return response.toResponse();
    }

    @GET
    @Path("/")
    @ApiOperation(value = "List objects")
    @Transactional
    public Response list(@QueryParam("page") int page, @QueryParam("per_page") int perPage, @QueryParam("filter") FieldFilter filter, @QueryParam("sort") Sort sort)
    {
        Paginator<T> paginator = new Paginator<>(getModelClass());

        Query query = null;
        if(filter != null)
        {
            query = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from " + getModelClass().getSimpleName() + " where " + filter.createQueryWherePart(null) + (sort != null ? (sort.createSortQueryPart(getModelClass())) : ""));
            filter.bindParams(query, getModelClass());
        }
        else if(sort != null)
        {
            query = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from " + getModelClass().getSimpleName() + sort.createSortQueryPart(getModelClass()));
        }

        Paginator.Page<T> _page = paginator.nextPage(page, perPage, query);

        ActivityManager.getInstance().add(request, null, "GET /" + getModelClass().getSimpleName().toLowerCase());

        MyResponse response = new MyResponse();
        response.setData(_page);
        return response.toResponse();
    }

    @PATCH
    @Path("/{id}")
    @ApiOperation(value = "Activate/Deactivate object. Omitting 'active' param will invert current value. This works only for objects extending HibernateModel")
    @Transactional()
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeState(@ApiParam(value = "id", required = true) @PathParam("id") long id, @ApiParam(value = "active", required = false) @Nullable @QueryParam("active") Boolean active)
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        T object = session.get(getModelClass(), id);

        if(object == null)
            throw new FBException(MyResponse.ErrorCode.not_found, 404, localeManager.getString("%s not found", request.getLocale(), getModelClass().getSimpleName()));


        if(object instanceof HibernateModel)
        {
            if(active == null)
                active = !((HibernateModel)object).isActive();
            ((HibernateModel)object).setActive(active);
        }
        else
        {
            throw new FBException(MyResponse.ErrorCode.server_error, 500, localeManager.getString("Model must extend HibernateModel to use ", request.getLocale()));
        }

        session.update(object);

        ActivityManager.getInstance().add(request, null, "PATCH /" + getModelClass().getSimpleName().toLowerCase() + "/");

        MyResponse response = new MyResponse();
        response.setData(object);
        return response.toResponse();
    }

    @DELETE
    @Path("/{id}")
    @ApiOperation(value = "Delete object.")
    @Transactional()
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(@ApiParam(value = "id", required = true) @PathParam("id") long id)
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        String hql = "delete " + getModelClass().getSimpleName() + " where id = :id";
        Query q = session.createQuery(hql).setParameter("id", id);
        int deleted = q.executeUpdate();

        ActivityManager.getInstance().add(request, null, "DELETE /" + getModelClass().getSimpleName().toLowerCase() + "/");

        MyResponse response = new MyResponse();
        response.setResponseMessage(localeManager.getString("%d %s deleted.", request.getLocale(), deleted, getModelClass().getSimpleName()));
        response.setData(deleted);
        return response.toResponse();
    }

    /**
     * Class of the model that this resource is querying in db. This must match Generic type*/
    @Nonnull
    protected abstract Class<T> getModelClass();

    @Nonnull
    protected String getNotFoundErrorKey()
    {
        return localeManager.getString("%s not found", request.getLocale(), getModelClass().getSimpleName());
    }

    public HttpServletRequest getRequest()
    {
        return request;
    }
}
