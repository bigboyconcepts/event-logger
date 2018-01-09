package com.newtecsolutions.floorball.resource;

import com.newtecsolutions.floorball.MyResponse;
import com.newtecsolutions.floorball.Paginator;
import com.newtecsolutions.floorball.intercept.Transactional;
import com.newtecsolutions.floorball.model.Permission;
import com.newtecsolutions.floorball.model.RegistrationCode;
import com.newtecsolutions.floorball.param_converter.filter.FieldFilter;
import com.newtecsolutions.floorball.param_converter.sort.Sort;
import com.newtecsolutions.floorball.utils.ActivityManager;
import com.newtecsolutions.floorball.utils.HibernateUtil;

import org.hibernate.query.Query;

import javax.annotation.Nonnull;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * Created by pedja on 6/29/17 10:43 AM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
@Path("/registration_code")
@Api("registration_code")
@Produces({MediaType.APPLICATION_JSON})
public class RegistrationCodeResource extends BaseResource<RegistrationCode>
{
    @Nonnull
    @Override
    protected Class<RegistrationCode> getModelClass()
    {
        return RegistrationCode.class;
    }

    @Override
    @Transactional(requiredPermissions = {Permission.view_back})
    public Response get(long id)
    {
        return super.get(id);
    }

    @Override
    @Transactional(requiredPermissions = {Permission.view_back})
    public Response list(int page, int perPage, FieldFilter filter, Sort sort)
    {
        Paginator<RegistrationCode> paginator = new Paginator<>(getModelClass());

        Query query;
        if (filter != null)
        {
            query = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from " + getModelClass().getSimpleName() + " rc join fetch rc.role join fetch rc.club where " + filter.createQueryWherePart("rc.") + (sort != null ? (sort.createSortQueryPart(getModelClass())) : ""));
            filter.bindParams(query, getModelClass());
        }
        else if (sort != null)
        {
            query = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from " + getModelClass().getSimpleName() + " rc join fetch rc.role join fetch rc.club " + sort.createSortQueryPart(getModelClass()));
        }
        else
        {
            query = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from " + getModelClass().getSimpleName() + " rc join fetch rc.role join fetch rc.club");
        }

        Paginator.Page<RegistrationCode> _page = paginator.nextPage(page, perPage, query);

        ActivityManager.getInstance().add(request, null, "GET /" + getModelClass().getSimpleName().toLowerCase());

        MyResponse response = new MyResponse();
        response.setData(_page);
        return response.toResponse();
    }

    @Override
    @Transactional(requiredPermissions = {Permission.view_back})
    public Response changeState(long id, Boolean active)
    {
        return super.changeState(id, active);
    }

    @Override
    @Transactional(requiredPermissions = {Permission.view_back})
    public Response delete(long id)
    {
        return super.delete(id);
    }

    @Override
    @Transactional(requiredPermissions = {Permission.view_back})
    public Response count()
    {
        return super.count();
    }
}
