package com.newtecsolutions.floorball.resource;

import com.newtecsolutions.floorball.FBException;
import com.newtecsolutions.floorball.MyResponse;
import com.newtecsolutions.floorball.Paginator;
import com.newtecsolutions.floorball.intercept.Transactional;
import com.newtecsolutions.floorball.model.Permission;
import com.newtecsolutions.floorball.model.RegistrationCode;
import com.newtecsolutions.floorball.model.Role;
import com.newtecsolutions.floorball.utils.ActivityManager;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
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

@Path("/role")
@Api("role")
@Produces({MediaType.APPLICATION_JSON})
public class RoleResource extends BaseResource<Role>
{
    @Context
    HttpServletRequest request;

    @GET
    @Path("/all")
    @ApiOperation(value = "List roles",
            response = Role.class)
    @Transactional(requiresAuth = false)
    public Response all(@QueryParam("page") int page, @QueryParam("per_page") int perPage, @QueryParam("filterPermissions")Set<Permission> permissions, @QueryParam("filterType") Role.FilterType filterType)
    {
        ActivityManager.getInstance().add(request, null, "GET /roles");

        List<Role> roles = Role.getRoles(permissions, filterType);
        Paginator.Page<Role> _page = new Paginator.Page<>(roles, roles.size(), 1);

        MyResponse response = new MyResponse();
        response.setData(_page);
        return response.toResponse();
    }

    @GET
    @Path("/by_registration_code")
    @ApiOperation(value = "Get role by registration code",
            response = Role.class)
    @Transactional(requiresAuth = false)
    public Response byRegistrationCode(@QueryParam("registrationCode") int registrationCode)
    {
        ActivityManager.getInstance().add(request, null, "GET /by_registration_code");

        RegistrationCode rcode = RegistrationCode.findByCode(registrationCode);

        if(rcode == null)
            throw new FBException(MyResponse.ErrorCode.not_found, 404, localeManager.getString("RegistrationCode not found", request.getLocale()));

        MyResponse response = new MyResponse();
        response.setData(rcode.getRole());
        return response.toResponse();
    }

    @Override
    @Transactional(requiredPermissions = {Permission.delete_role})
    public Response delete(long id)
    {
        return super.delete(id);
    }

    @Override
    @Transactional(requiredPermissions = {Permission.update_role})
    public Response changeState(long id, Boolean active)
    {
        return super.changeState(id, active);
    }

    @Nonnull
    @Override
    protected Class<Role> getModelClass()
    {
        return Role.class;
    }

    public HttpServletRequest getRequest()
    {
        return request;
    }
}