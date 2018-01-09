package com.newtecsolutions.floorball.resource;

import com.newtecsolutions.floorball.FBException;
import com.newtecsolutions.floorball.MyResponse;
import com.newtecsolutions.floorball.intercept.Transactional;
import com.newtecsolutions.floorball.model.CampTrainer;
import com.newtecsolutions.floorball.model.File;
import com.newtecsolutions.floorball.model.Permission;
import com.newtecsolutions.floorball.param_converter.filter.FieldFilter;
import com.newtecsolutions.floorball.param_converter.sort.Sort;
import com.newtecsolutions.floorball.utils.ActivityManager;
import com.newtecsolutions.floorball.utils.HibernateUtil;
import com.newtecsolutions.floorball.utils.LocaleManager;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.Session;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

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
 * Created by pedja on 7/4/17 7:46 AM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */

@Path("/camp_trainer")
@Api("camp_trainer")
@Produces({MediaType.APPLICATION_JSON})
public class CampTrainerResource extends BaseResource<CampTrainer>
{
    @Override
    @Transactional()
    public Response get(long id)
    {
        return getCampTrainer(false);
    }

    private Response getCampTrainer(boolean asList)
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        CampTrainer object = (CampTrainer) session.createQuery("from CampTrainer ct left join fetch ct.instructions where ct.id = :id").setParameter("id", CampTrainer.CAMP_TRAINER_ID).getSingleResult();

        ActivityManager.getInstance().add(request, null, "GET /" + getModelClass().getSimpleName().toLowerCase() + "/{id}");

        if (object == null)
        {
            return MyResponse.errorResponse(MyResponse.ErrorCode.not_found, getNotFoundErrorKey());
        }

        MyResponse response = new MyResponse();
        response.setData(asList ? Collections.singletonList(object) : object);
        return response.toResponse();
    }

    @Override
    @Transactional()
    public Response count()
    {
        return super.count();
    }

    @Override
    @Transactional()
    public Response list(int page, int perPage, FieldFilter filter, Sort sort)
    {
        return getCampTrainer(true);
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

    @POST
    @Path("/pdf")
    @ApiOperation(value = "Add pdf instructions")
    @Transactional(requiredPermissions = {Permission.create_track})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addImage(@Nonnull @ApiParam(value = "Pdf instructions", required = true) @FormDataParam("pdf") InputStream pdfInputStream,
                             @Nonnull @FormDataParam("pdf") FormDataContentDisposition pdfMetaData)
    {
        if (!File.isPdf(pdfMetaData))
            throw new FBException(MyResponse.ErrorCode.api_error, 400, localeManager.getString("Unsupported file for 'pdf' param", request.getLocale()));

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        CampTrainer ct = session.get(CampTrainer.class, CampTrainer.CAMP_TRAINER_ID);

        if (ct == null)
        {
            ct = new CampTrainer();
            ct.setId(CampTrainer.CAMP_TRAINER_ID);
        }


        File file = File.uploadFile(ct, pdfInputStream, pdfMetaData, localeManager);
        ct.setInstructions(file);
        session.save(ct);

        MyResponse response = new MyResponse();
        response.setData(ct);
        return response.toResponse();
    }

    @Nonnull
    @Override
    protected Class<CampTrainer> getModelClass()
    {
        return CampTrainer.class;
    }
}
