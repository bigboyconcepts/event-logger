package com.newtecsolutions.floorball.resource;

import com.newtecsolutions.floorball.FBException;
import com.newtecsolutions.floorball.MyResponse;
import com.newtecsolutions.floorball.Paginator;
import com.newtecsolutions.floorball.intercept.Transactional;
import com.newtecsolutions.floorball.model.Club;
import com.newtecsolutions.floorball.model.File;
import com.newtecsolutions.floorball.model.Member;
import com.newtecsolutions.floorball.model.Permission;
import com.newtecsolutions.floorball.param_converter.filter.FieldFilter;
import com.newtecsolutions.floorball.param_converter.sort.Sort;
import com.newtecsolutions.floorball.utils.ActivityManager;
import com.newtecsolutions.floorball.utils.HibernateUtil;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.skynetsoftware.jutils.StringUtils;

import java.io.InputStream;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Created by pedja on 6/29/17 10:43 AM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
@Path("/club")
@Api("club")
@Produces({MediaType.APPLICATION_JSON})
public class ClubResource extends BaseResource<Club>
{
    @Nonnull
    @Override
    protected Class<Club> getModelClass()
    {
        return Club.class;
    }

    @Override
    @Transactional(requiredPermissions = {Permission.appmode_club_basic, Permission.appmode_home_trainer})
    public Response get(long id)
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        Club club = session.get(getModelClass(), id);

        ActivityManager.getInstance().add(request, null, "GET /" + getModelClass().getSimpleName().toLowerCase() + "/{id}");

        if (club == null)
        {
            return MyResponse.errorResponse(MyResponse.ErrorCode.not_found, getNotFoundErrorKey());
        }
        Set<Member> members = club.getMembers();

        //detach all members from hibernate, and set club to null to avoid circular dependency
        //circular dependency will cause stack overflow while serializing
        for (Member member : members)
        {
            session.detach(member);
            member.setClub(null);
        }

        MyResponse response = new MyResponse();
        response.setData(club);
        return response.toResponse();
    }

    @Override
    @Transactional(requiredPermissions = {Permission.appmode_club_basic, Permission.appmode_home_trainer})
    public Response list(int page, int perPage, FieldFilter filter, Sort sort)
    {
        Paginator<Club> paginator = new Paginator<>(getModelClass());

        Query query;
        if (filter != null)
        {
            query = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from " + getModelClass().getSimpleName() + " club left join fetch club.logo where " + filter.createQueryWherePart("club.") + (sort != null ? (sort.createSortQueryPart(getModelClass())) : ""));
            filter.bindParams(query, getModelClass());
        }
        else if (sort != null)
        {
            query = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from " + getModelClass().getSimpleName() + " club left join fetch club.logo " + sort.createSortQueryPart(getModelClass()));
        }
        else
        {
            query = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from " + getModelClass().getSimpleName() + " club left join fetch club.logo");
        }
        Paginator.Page<Club> _page = paginator.nextPage(page, perPage, query);

        ActivityManager.getInstance().add(request, null, "GET /" + getModelClass().getSimpleName().toLowerCase());

        MyResponse response = new MyResponse();
        response.setData(_page);
        return response.toResponse();
    }

    @POST
    @Path("/")
    @ApiOperation(value = "Create club")
    @Transactional(requiredPermissions = {Permission.create_club})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response create(@ApiParam(value = "Name of the club", required = true) @FormDataParam("name") String name,
                           @Nonnull @ApiParam(value = "Club photo", required = true) @FormDataParam("image") InputStream imageInputStream,
                           @Nonnull @FormDataParam("image") FormDataContentDisposition imageMetaData)
    {
        if (!File.isImage(imageMetaData))
            throw new FBException(MyResponse.ErrorCode.api_error, 400, localeManager.getString("Unsupported file for 'image' param", request.getLocale()));

        Club club = Club.createClub(name, imageInputStream, imageMetaData, localeManager);

        MyResponse response = new MyResponse();
        response.setData(club);
        return response.toResponse();
    }

    @PUT
    @Path("/{id}")
    @ApiOperation(value = "Update club.")
    @Transactional(requiredPermissions = {Permission.update_club})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response update(@PathParam("id") long id, @FormParam("name") String name)
    {
        ActivityManager.getInstance().add(request, null, "PUT /club/{id}");
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Club club = session.get(Club.class, id);
        if (club == null)
            throw new FBException(MyResponse.ErrorCode.not_found, 404, localeManager.getString("Club not found", request.getLocale()));
        if (!StringUtils.isEmpty(name))
            club.setName(name);
        session.save(club);

        MyResponse response = new MyResponse();
        response.setData(club);
        return response.toResponse();
    }

    @Override
    @Transactional(requiredPermissions = {Permission.update_club})
    public Response changeState(long id, Boolean active)
    {
        return super.changeState(id, active);
    }

    @Override
    @Transactional(requiredPermissions = {Permission.delete_club})
    public Response delete(long id)
    {
        return super.delete(id);
    }

    @Override
    @Transactional(requiredPermissions = {Permission.appmode_club_basic, Permission.appmode_home_trainer})
    public Response count()
    {
        return super.count();
    }
}
