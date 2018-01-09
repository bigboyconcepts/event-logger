package com.newtecsolutions.floorball.resource;

import com.newtecsolutions.floorball.FBException;
import com.newtecsolutions.floorball.MyResponse;
import com.newtecsolutions.floorball.Paginator;
import com.newtecsolutions.floorball.intercept.MyResourceInterceptor;
import com.newtecsolutions.floorball.intercept.Transactional;
import com.newtecsolutions.floorball.model.File;
import com.newtecsolutions.floorball.model.Member;
import com.newtecsolutions.floorball.model.OAuthAccessToken;
import com.newtecsolutions.floorball.model.Permission;
import com.newtecsolutions.floorball.model.Track;
import com.newtecsolutions.floorball.model.TrackActionRegion;
import com.newtecsolutions.floorball.param_converter.app_mode.AppModeParam;
import com.newtecsolutions.floorball.param_converter.filter.FieldFilter;
import com.newtecsolutions.floorball.param_converter.sort.Sort;
import com.newtecsolutions.floorball.utils.ActivityManager;
import com.newtecsolutions.floorball.utils.HibernateUtil;
import com.newtecsolutions.floorball.utils.LocaleManager;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
@Path("/track")
@Api("track")
@Produces({MediaType.APPLICATION_JSON})
public class TrackResource extends BaseResource<Track>
{
    @Nonnull
    @Override
    protected Class<Track> getModelClass()
    {
        return Track.class;
    }

    @Override
    @Transactional(requiredPermissions = {Permission.appmode_club_basic, Permission.appmode_home_trainer})
    public Response get(long id)
    {
        Track track = Track.fetchTrackEager(id);

        ActivityManager.getInstance().add(request, null, "GET /track/{id}");

        if (track == null)
        {
            return MyResponse.errorResponse(MyResponse.ErrorCode.not_found, getNotFoundErrorKey());
        }

        MyResponse response = new MyResponse();
        response.setData(track);
        return response.toResponse();
    }

    @Override
    @Transactional(requiredPermissions = {Permission.appmode_club_basic, Permission.appmode_home_trainer})
    public Response list(int page, int perPage, FieldFilter filter, Sort sort)
    {
        Paginator<Track> paginator = new Paginator<>(getModelClass());

        Query query;
        if(filter != null)
        {
            query = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from " + getModelClass().getSimpleName() + " track inner join fetch track.trackImage where " + filter.createQueryWherePart("track.") + (sort != null ? (sort.createSortQueryPart(getModelClass())) : ""));
            filter.bindParams(query, getModelClass());
        }
        else if(sort != null)
        {
            query = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from " + getModelClass().getSimpleName() + " track inner join fetch track.trackImage " + sort.createSortQueryPart(getModelClass()));
        }
        else
        {
            query = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from " + getModelClass().getSimpleName() + " track inner join fetch track.trackImage ");
        }

        Paginator.Page<Track> _page = paginator.nextPage(page, perPage, query);

        ActivityManager.getInstance().add(request, null, "GET /" + getModelClass().getSimpleName().toLowerCase());

        MyResponse response = new MyResponse();
        response.setData(_page);
        return response.toResponse();
    }

    @POST
    @Path("/")
    @ApiOperation(value = "Create track")
    @Transactional(requiredPermissions = {Permission.create_track})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response create(@ApiParam(value = "Name of the track", required = true) @FormDataParam("name") String name,
                           @Nonnull @ApiParam(value = "App modes", required = true) @FormDataParam("appModes") AppModeParam appModes)
    {
        if (appModes.getAppModes().isEmpty())
            throw new FBException(MyResponse.ErrorCode.api_error, 400, localeManager.getString("appModes cannot be empty", request.getLocale()));

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Track track = new Track();
        track.setName(name);
        track.setAppModeList(appModes.getAppModes());

        session.save(track);

        MyResponse response = new MyResponse();
        response.setData(track);
        return response.toResponse();
    }

    @POST
    @Path("/{id}/image")
    @ApiOperation(value = "Add track image")
    @Transactional(requiredPermissions = {Permission.create_track})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addImage(@PathParam("id") long id,
                           @Nonnull @ApiParam(value = "Track photo", required = true) @FormDataParam("image") InputStream imageInputStream,
                           @Nonnull @FormDataParam("image") FormDataContentDisposition imageMetaData)
    {
        if (!File.isImage(imageMetaData))
            throw new FBException(MyResponse.ErrorCode.api_error, 400, localeManager.getString("Unsupported file for 'image' param", request.getLocale()));

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Track track = session.get(Track.class, id);

        if (track == null)
            throw new FBException(MyResponse.ErrorCode.not_found, 404, localeManager.getString("Track not found", request.getLocale()));

        File file = File.uploadFile(track, imageInputStream, imageMetaData, localeManager);
        track.setTrackImage(file);
        session.save(track);

        MyResponse response = new MyResponse();
        response.setData(track);
        return response.toResponse();
    }

    @POST
    @Path("/{id}/video")
    @ApiOperation(value = "Add track video")
    @Transactional(requiredPermissions = {Permission.create_track})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addVideo(@PathParam("id") long id,
                           @ApiParam(value = "Track image region x for this video", required = true) @FormDataParam("clickRegionX") int clickRegionX,
                           @ApiParam(value = "Track image region y for this video", required = true) @FormDataParam("clickRegionY") int clickRegionY,
                           @Nonnull @ApiParam(value = "Track video", required = true) @FormDataParam("video") InputStream videoInputStream,
                           @FormDataParam("video") FormDataContentDisposition videoMetaData)
    {
        if (videoMetaData != null && !File.isVideo(videoMetaData))
            throw new FBException(MyResponse.ErrorCode.api_error, 400, localeManager.getString("Unsupported file for 'video' param", request.getLocale()));

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        Track track = session.get(Track.class, id);

        if (track == null)
            throw new FBException(MyResponse.ErrorCode.not_found, 404, localeManager.getString("Track not found", request.getLocale()));

        File video = File.uploadFile(track, videoInputStream, videoMetaData, localeManager);
        if (track.getTrackVideos() == null)
            track.setTrackVideos(new HashSet<>());
        track.getTrackVideos().add(video);

        TrackActionRegion region = new TrackActionRegion();
        region.setX(clickRegionX);
        region.setY(clickRegionY);
        region.setTrack(track);
        region.setVideo(video);
        session.save(region);

        MyResponse response = new MyResponse();
        response.setData(track);
        return response.toResponse();
    }

    @POST
    @Path("/session")
    @ApiOperation(value = "Create session")
    @Transactional(requiredPermissions = {Permission.appmode_home_trainer, Permission.appmode_camp_training, Permission.create_session})
    public Response createSession(@Nonnull @ApiParam(value = "Track ids", required = true) @FormParam("trackIds") List<Long> trackIds)
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Member member = ((OAuthAccessToken)request.getAttribute(MyResourceInterceptor.ATTR_TOKEN)).getMember();

        member.getTracks().clear();

        Query query = session.createQuery("from Track where id in (:ids)");
        query.setParameter("ids", trackIds);
        List<Track> tracks = query.getResultList();
        member.getTracks().addAll(tracks);

        MyResponse response = new MyResponse();
        response.setData(tracks);
        return response.toResponse();
    }

    @GET
    @Path("/session/{trainer_id}")
    @ApiOperation(value = "List sessions for track")
    @Transactional()
    public Response getSession(@PathParam("trainer_id") long trainerId)
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Member member = session.get(Member.class, trainerId);
        if(member == null)
            throw new FBException(MyResponse.ErrorCode.not_found, 404, localeManager.getString("Member not found", request.getLocale()));

        Query query = session.createQuery("from Member mmbr inner join fetch mmbr.tracks tracks inner join fetch tracks.trackImage where mmbr = :mmbr");
        query.setParameter("mmbr", member);

        try
        {
            member = (Member) query.getSingleResult();

            MyResponse response = new MyResponse();

            List<Track> tracks = new ArrayList<>(member.getTracks());
            tracks.sort(new Track.NameComparator());

            response.setData(tracks);
            return response.toResponse();
        }
        catch (NoResultException e)
        {
            MyResponse response = new MyResponse();
            response.setData(Collections.emptyList());
            return response.toResponse();
        }
    }

    @Override
    @Transactional(requiredPermissions = {Permission.update_track})
    public Response changeState(long id, Boolean active)
    {
        return super.changeState(id, active);
    }

    @Override
    @Transactional(requiredPermissions = {Permission.delete_track})
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
