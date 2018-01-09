package com.newtecsolutions.floorball.resource;

import com.newtecsolutions.floorball.Consts;
import com.newtecsolutions.floorball.FBException;
import com.newtecsolutions.floorball.MyResponse;
import com.newtecsolutions.floorball.Paginator;
import com.newtecsolutions.floorball.auth.MyOAuthTokenRequest;
import com.newtecsolutions.floorball.auth.OAuthRequestWrapper;
import com.newtecsolutions.floorball.auth.OAuthUtils;
import com.newtecsolutions.floorball.input_validator.InputValidatorError;
import com.newtecsolutions.floorball.input_validator.SimpleInputValidator;
import com.newtecsolutions.floorball.intercept.MyResourceInterceptor;
import com.newtecsolutions.floorball.intercept.Transactional;
import com.newtecsolutions.floorball.mail.PostOffice;
import com.newtecsolutions.floorball.model.Club;
import com.newtecsolutions.floorball.model.File;
import com.newtecsolutions.floorball.model.Member;
import com.newtecsolutions.floorball.model.OAuthAccessToken;
import com.newtecsolutions.floorball.model.OAuthClient;
import com.newtecsolutions.floorball.model.OAuthRefreshToken;
import com.newtecsolutions.floorball.model.Permission;
import com.newtecsolutions.floorball.model.RegistrationCode;
import com.newtecsolutions.floorball.model.Role;
import com.newtecsolutions.floorball.param_converter.filter.FieldFilter;
import com.newtecsolutions.floorball.param_converter.sort.Sort;
import com.newtecsolutions.floorball.utils.ActivityManager;
import com.newtecsolutions.floorball.utils.ConfigManager;
import com.newtecsolutions.floorball.utils.HibernateUtil;
import com.newtecsolutions.floorball.utils.LocaleManager;
import com.newtecsolutions.floorball.utils.LogUtils;
import com.newtecsolutions.floorball.utils.PasswordUtils;
import com.newtecsolutions.floorball.utils.RandStringGenerator;
import com.newtecsolutions.floorball.utils.Utils;

import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.mvc.Viewable;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.skynetsoftware.jutils.StringUtils;

import java.io.InputStream;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("/member")
@Api("member")
@Produces({MediaType.APPLICATION_JSON})
public class MemberResource extends BaseResource<Member>
{
    @Inject
    private SimpleInputValidator simpleInputValidator;

    /**
     * <pre>
     * Every member can login regardless of role
     * After login role will be checked for permission to view front (front = mobile app) or back
     * </pre>
     * */
    @POST
    @Path("/token")
    @ApiOperation(value = "Login to the server using oauth",
            response = Member.class)
    @Transactional(requiresAuth = false)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "Username", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "password", value = "Password", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "client_id", value = "Client Id", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "client_secret", value = "Client Secret", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "grant_type", value = "Grant Type", required = true, dataType = "string", paramType = "form"),
    })
    @Consumes("application/x-www-form-urlencoded")
    public Response login(MultivaluedMap<String, String> form)
    {
        ActivityManager.getInstance().add(request, null, "POST /members/token");
        //http://blogs.steeplesoft.com/posts/2013/a-simple-oauth2-client-and-server-example-part-i.html
        try
        {
            OAuthTokenRequest oauthRequest = new MyOAuthTokenRequest(new OAuthRequestWrapper(request, form));
            OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

            Session session = HibernateUtil.getSessionFactory().getCurrentSession();

            OAuthClient client;
            // check if clientId and secret are valid
            if ((client = OAuthClient.getClient(oauthRequest.getClientId(), oauthRequest.getClientSecret())) == null)
            {
                return MyResponse.errorResponse(MyResponse.ErrorCode.oauth_error, localeManager.getString("Invalid client credentials", request.getLocale()));
            }

            List<String> grantTypes = Utils.deserializeStringList(client.getGrantTypes());

            Member member;
            if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.PASSWORD.toString()))
            {
                if (!grantTypes.contains(GrantType.PASSWORD.toString()))
                {
                    return MyResponse.errorResponse(MyResponse.ErrorCode.oauth_error, String.format(localeManager.getString("'%s' type not granted", request.getLocale()), GrantType.PASSWORD.toString()));
                }
                if ((member = Member.checkUserPass(oauthRequest.getUsername(), oauthRequest.getPassword())) == null)
                {
                    return MyResponse.errorResponse(MyResponse.ErrorCode.oauth_error, localeManager.getString("Wrong username or password", request.getLocale()), 422);
                }
            }
            else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.REFRESH_TOKEN.toString()))
            {
                if (!grantTypes.contains(GrantType.REFRESH_TOKEN.toString()))
                {
                    return MyResponse.errorResponse(MyResponse.ErrorCode.oauth_error, String.format(localeManager.getString("'%s' type not granted", request.getLocale()), GrantType.REFRESH_TOKEN.toString()));
                }
                if (OAuthRefreshToken.getRefreshToken(oauthRequest.getRefreshToken()) == null)
                {
                    return MyResponse.errorResponse(MyResponse.ErrorCode.oauth_error, localeManager.getString("Invalid refresh token", request.getLocale()));
                }
                if ((member = Member.checkUser(oauthRequest.getUsername())) == null)
                {
                    return MyResponse.errorResponse(MyResponse.ErrorCode.oauth_error, localeManager.getString("Wrong username or password", request.getLocale()), 422);
                }
            }
            else
            {
                return MyResponse.errorResponse(MyResponse.ErrorCode.oauth_error, String.format(localeManager.getString("'%s' type not granted", request.getLocale()), oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE)));
            }

            Role role = member.getRole();
            if(role == null)
                return MyResponse.errorResponse(MyResponse.ErrorCode.oauth_error, localeManager.getString("Wrong username or password", request.getLocale()), 422);

            OAuthAccessToken accessToken = new OAuthAccessToken();
            accessToken.setAccessToken(oauthIssuerImpl.accessToken());
            accessToken.setClient(client);
            accessToken.setMember(member);
            long nowMillis = System.currentTimeMillis();
            accessToken.setExpires(new Date(nowMillis + ConfigManager.getInstance().getLong(ConfigManager.CONFIG_OAUTH_ACCESS_TOKEN_EXPIRES_NAME, OAuthAccessToken.DEFAULT_EXPIRES_MILLIS)));
            accessToken.setScope(OAuthUtils.oauthScopesSetToString(oauthRequest.getScopes()));
            session.save(accessToken);

            OAuthRefreshToken refreshToken = new OAuthRefreshToken();
            refreshToken.setRefreshToken(oauthIssuerImpl.refreshToken());
            refreshToken.setClient(client);
            refreshToken.setMember(member);
            refreshToken.setExpires(new Date(nowMillis + ConfigManager.getInstance().getLong(ConfigManager.CONFIG_OAUTH_REFRESH_TOKEN_EXPIRES_NAME, OAuthRefreshToken.DEFAULT_EXPIRES_MILLIS)));
            refreshToken.setScope(OAuthUtils.oauthScopesSetToString(oauthRequest.getScopes()));
            session.save(refreshToken);

            Hibernate.initialize(member.getRole());

            Map<String, Object> map = new HashMap<>();
            map.put("member", member);
            map.put("accessToken", accessToken.getAccessToken());
            map.put("refreshToken", refreshToken.getRefreshToken());
            map.put("expiresIn", ConfigManager.getInstance().getLong(ConfigManager.CONFIG_OAUTH_ACCESS_TOKEN_EXPIRES_NAME, OAuthAccessToken.DEFAULT_EXPIRES_MILLIS));

            MyResponse pResponse = new MyResponse();
            pResponse.setData(map);
            return pResponse.toResponse();
        }
        catch (OAuthProblemException | OAuthSystemException e)
        {
            LogUtils.info(e.getMessage());
            return MyResponse.errorResponse(MyResponse.ErrorCode.oauth_error, e.getMessage());
        }
    }

    @POST
    @Path("/register")
    @ApiOperation(value = "Register member",
            response = Member.class)
    @Transactional(requiresAuth = false)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response register(@Context UriInfo uriInfo,
                             @ApiParam(value = "Registration code", required = true) @FormDataParam("registrationCode") int registrationCode,
                             @Nonnull @ApiParam(value = "Email", required = true) @FormDataParam("email") String email,
                             @Nonnull @ApiParam(value = "Password", required = true) @FormDataParam("password") String password,
                             @Nonnull @ApiParam(value = "First Name", required = true) @FormDataParam("firstName") String firstName,
                             @Nonnull @ApiParam(value = "Last Name", required = true) @FormDataParam("lastName") String lastName,
                             @ApiParam(value = "Date of birth. Is required depends on role. milliseconds", required = false) @FormDataParam("dateOfBirth") Long dateOfBirth,
                             @ApiParam(value = "Club photo", required = false) @FormDataParam("image") InputStream imageInputStream,
                             @FormDataParam("image") FormDataContentDisposition imageMetaData)
    {
        if (imageInputStream != null && !File.isImage(imageMetaData))
            throw new FBException(MyResponse.ErrorCode.api_error, 400, localeManager.getString("Unsupported file for 'image' param", request.getLocale()));

        try
        {
            RegistrationCode rcode = RegistrationCode.findByCode(registrationCode);

            if(rcode == null || rcode.getRole() == null)
                throw new FBException(MyResponse.ErrorCode.not_found, 404, localeManager.getString("RegistrationCode not found", request.getLocale()));

            Role role = rcode.getRole();

            if(!role.getPermissionsList().contains(Permission.can_register))
                throw new InputValidatorError(localeManager.getString("Registration as '%s' is not allowed", request.getLocale(), role.getName()));

            simpleInputValidator.checkEmailValid(email, request.getLocale());
            simpleInputValidator.checkEmpty("password", password, request.getLocale());
            simpleInputValidator.checkEmpty("firstName", firstName, request.getLocale());
            simpleInputValidator.checkEmpty("lastName", lastName, request.getLocale());

            if(role.getPermissionsList().contains(Permission.parent) || role.getPermissionsList().contains(Permission.student))
            {
                if(dateOfBirth == null)
                {
                    throw new InputValidatorError(localeManager.getString("%s cannot be empty", request.getLocale(), "dateOfBirth"));
                }
            }

            Session session = HibernateUtil.getSessionFactory().getCurrentSession();

            Member tmp = Member.findMemberByField("email", email);
            if (tmp != null)
            {
                throw new InputValidatorError(localeManager.getString("Email address is already taken", request.getLocale()));
            }

            Member member = new Member();

            member.setEmail(email);
            member.setFirstName(firstName);
            member.setLastName(lastName);
            member.setPasswordHash(PasswordUtils.generateStrongPasswordHash(password));
            member.setDataOfBirth(new Date(dateOfBirth == null ? 0 : dateOfBirth));

            member.setStatus(Member.Status.pending);
            member.setVerificationToken(RandStringGenerator.nextString(32));

            member.setRole(role);
            member.setClub(rcode.getClub());

            if (imageInputStream != null)
            {
                File file = File.uploadFile(member, imageInputStream, imageMetaData, localeManager);
                member.setAvatar(file);
            }

            Serializable ret = session.save(member);
            ActivityManager.getInstance().add(request, member, "GET /member/register");
            if (ret != null)
            {
                PostOffice.getInstance().sendEmailVerificationMail(uriInfo.getBaseUri().toString(), member, localeManager, request.getLocale());
                MyResponse response = new MyResponse();
                response.setResponseMessage(localeManager.getString("Registration Successful. Check your email for verification.", request.getLocale()));
                return response.toResponse();
            }
            else
            {
                throw new InputValidatorError(localeManager.getString("Failed to register member", request.getLocale()));
            }
        }
        catch (InputValidatorError e)
        {
            LogUtils.info(e.getMessage());
            MyResponse response = new MyResponse();
            response.addError(MyResponse.ErrorCode.invalid_input, e.getMessage());
            return response.toResponse();
        }
        catch (InvalidKeySpecException | NoSuchAlgorithmException e)
        {
            LogUtils.info(e.getMessage());
            MyResponse response = new MyResponse();
            response.addError(MyResponse.ErrorCode.server_error, e.getMessage());
            return response.toResponse();
        }
    }

    @GET
    @Path("/reset_password")
    @ApiOperation(value = "Reset Password, New (random) password will be sent to users email address")
    @Transactional()
    public Response resetPassword(@Context UriInfo uriInfo, @ApiParam(value = "Users email address", required = true) @QueryParam("email") String email)
    {
        ActivityManager.getInstance().add(request, null, "GET /members/reset_password");

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        Member member = Member.findMemberByField("email", email);
        if (member == null)
        {
            throw new FBException(localeManager.getString("No member found for that email address", request.getLocale()));
        }

        member.setResetPasswordToken(RandStringGenerator.nextString(32));

        session.save(member);
        PostOffice.getInstance().sendResetPasswordMail(uriInfo.getBaseUri().toString(), member, localeManager, request.getLocale());
        MyResponse response = new MyResponse();
        response.setResponseMessage(localeManager.getString("Check your email for instructions on how to reset password.", request.getLocale()));
        return response.toResponse();
    }

    @GET
    @Path("/new_password/{member_id}/{token}")
    @Produces("text/html")
    @ApiOperation(value = "Send new password, called from email client",
            response = Member.class)
    @Transactional
    public Response newPassword(@Context UriInfo uriInfo, @ApiParam(value = "Member id", required = true) @PathParam("member_id") long memberId,
                                @ApiParam(value = "Password reset token token", required = true) @PathParam("token") String token)
    {
        ActivityManager.getInstance().add(request, null, "GET /members/new_password");
        Member member = null;
        try
        {
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();

            member = session.get(Member.class, memberId);

            if (!StringUtils.isEmpty(token) && token.equals(member.getResetPasswordToken()))
            {
                String passwordPlain;
                member.setPasswordHash(PasswordUtils.generateStrongPasswordHash(passwordPlain = RandStringGenerator.nextString(8)));
                member.setPassword(null);
                member.setResetPasswordToken(null);

                session.save(member);
                PostOffice.getInstance().sendNewPasswordMail(uriInfo.getBaseUri().toString(), passwordPlain, member, localeManager, request.getLocale());
            }
            else
            {
                member = null;
            }

        }
        catch (InvalidKeySpecException | NoSuchAlgorithmException e)
        {
            LogUtils.info(e.getMessage());
        }

        String baseUrl = uriInfo.getBaseUri().toString();

        Map<String, Object> map = new HashMap<>();
        map.put("member", member);
        map.put("status", member != null);
        map.put("base_url", baseUrl.substring(0, baseUrl.length() - 4));
        return Response.ok(new Viewable("/verify/reset_password", map)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/change_password")
    @ApiOperation(value = "Change password, all user access tokens will be deleted on success")
    @Transactional
    public Response changePassword(@ApiParam(value = "Current password", required = true)
                                   @Nonnull @FormParam("currentPassword") String currentPassword,
                                   @ApiParam(value = "New password", required = true)
                                   @Nonnull @FormParam("newPassword") String newPassword)
    {
        try
        {
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            OAuthAccessToken token = (OAuthAccessToken) request.getAttribute(MyResourceInterceptor.ATTR_TOKEN);

            Member member = token.getMember();
            ActivityManager.getInstance().add(request, member, "GET /members/change_password");

            if (StringUtils.isEmpty(currentPassword) || StringUtils.isEmpty(newPassword) || !PasswordUtils.validatePassword(currentPassword, member.getPasswordHash()))
            {
                throw new FBException(localeManager.getString("Wrong password", request.getLocale()));
            }

            member.setPasswordHash(PasswordUtils.generateStrongPasswordHash(newPassword));
            session.save(member);

            Query query = session.createQuery("delete from OAuthRefreshToken token where token.member = :member");
            query.setParameter("member", member);
            query.executeUpdate();
            query = session.createQuery("delete from OAuthAccessToken token where token.member = :member");
            query.setParameter("member", member);
            query.executeUpdate();

            MyResponse response = new MyResponse();
            response.setResponseMessage(localeManager.getString("Password updated. Please login with the new password", request.getLocale()));
            return response.toResponse();
        }
        catch (InvalidKeySpecException | NoSuchAlgorithmException e)
        {
            LogUtils.info(e.getMessage());
            MyResponse response = new MyResponse();
            response.addError(MyResponse.ErrorCode.server_error, e.getMessage());
            return response.toResponse();
        }
    }

    @PUT
    @Path("/{id}")
    @ApiOperation(value = "Update member.")
    public Response update(@PathParam("id") long id, Member member)
    {
        ActivityManager.getInstance().add(request, member, "POST /members/{id}");
        try
        {
            if (member == null)
            {
                throw new InputValidatorError(localeManager.getString("Missing input data", request.getLocale()));
            }

            simpleInputValidator.checkEmpty("first_name", member.getFirstName(), request.getLocale());
            simpleInputValidator.checkEmpty("last_name", member.getLastName(), request.getLocale());
            simpleInputValidator.checkEmpty("username", member.getLastName(), request.getLocale());

            OAuthAccessToken token = (OAuthAccessToken) request.getAttribute(MyResourceInterceptor.ATTR_TOKEN);

            Session session = HibernateUtil.getSessionFactory().getCurrentSession();

            Member caller = token.getMember();

            Member dbMember = session.get(Member.class, id);

            if(dbMember == null)
                throw new FBException(MyResponse.ErrorCode.not_found, 404, localeManager.getString("Member not found", request.getLocale()));

            Member.checkPermissionTo(session, Permission.update_member, caller, dbMember, null, request, localeManager);

            dbMember.setProfileDataFrom(member);

            session.save(dbMember);
            MyResponse response = new MyResponse();
            response.setResponseMessage(localeManager.getString("Profile saved", request.getLocale()));
            response.setData(dbMember);
            return response.toResponse();
        }
        catch (InputValidatorError e)
        {
            LogUtils.info(e.getMessage());
            MyResponse response = new MyResponse();
            response.addError(MyResponse.ErrorCode.invalid_input, e.getMessage());
            return response.toResponse();
        }
    }

    @Override
    @ApiOperation(value = "Delete member. Delete from database, only SuperAdmin role has this permission")
    public Response delete(long id)
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        OAuthAccessToken token = (OAuthAccessToken) request.getAttribute(MyResourceInterceptor.ATTR_TOKEN);
        Member caller = token.getMember();
        Member target = session.get(Member.class, id);

        if(target == null)
            throw new FBException(MyResponse.ErrorCode.not_found, 404, localeManager.getString("Member not found", request.getLocale()));

        Member.checkPermissionTo(HibernateUtil.getSessionFactory().getCurrentSession(), Permission.delete_member, caller, target, null, request, localeManager);

        session.delete(target);

        ActivityManager.getInstance().add(request, null, "DELETE /member");

        MyResponse response = new MyResponse();
        return response.toResponse();
    }

    @Override
    public Response changeState(long id, Boolean active)
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        Member object = session.get(getModelClass(), id);

        if(object == null)
            throw new FBException(MyResponse.ErrorCode.not_found, 404, localeManager.getString("%s not found", request.getLocale(), getModelClass().getSimpleName()));

        OAuthAccessToken token = (OAuthAccessToken) request.getAttribute(MyResourceInterceptor.ATTR_TOKEN);
        Member caller = token.getMember();

        Member.checkPermissionTo(HibernateUtil.getSessionFactory().getCurrentSession(), Permission.update_member, caller, object, null, request, localeManager);

        if(active == null)
            active = !object.isActive();
        object.setActive(active);

        session.update(object);

        ActivityManager.getInstance().add(request, null, "PATCH /" + getModelClass().getSimpleName().toLowerCase() + "/");

        MyResponse response = new MyResponse();
        response.setData(object);
        return response.toResponse();
    }

    @GET
    @Path("/verify/{member_id}/{token}")
    @Produces("text/html")
    @ApiOperation(value = "Verify email, called from email client",
            response = Member.class)
    @Transactional(requiresAuth = false)
    public Response verify(@Context UriInfo uriInfo, @ApiParam(value = "Member id", required = true) @PathParam("member_id") long memberId, @ApiParam(value = "Verification token", required = true) @PathParam("token") String token)
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Member member = session.get(Member.class, memberId);
        ActivityManager.getInstance().add(request, member, "GET /members/{member_id}/{token}");

        boolean success = true;
        if (member == null || StringUtils.isEmpty(token) || !token.equals(member.getVerificationToken()))
        {
            LogUtils.info("tokeni nisu isti ili nesto drugo");
            success = false;
        }
        else
        {
            LogUtils.info("usao sam ovde");
            member.setStatus(Member.Status.active);
            member.setVerificationToken(null);
            session.save(member);
        }
        LogUtils.info(String.valueOf(success));

        String baseUrl = uriInfo.getBaseUri().toString();

        Map<String, Object> map = new HashMap<>();
        map.put("member", member);
        map.put("status", success);
        map.put("base_url", baseUrl.substring(0, baseUrl.length() - 4));
        return Response.ok(new Viewable("/verify/verify", map)).build();
    }

    @Override
    public Response list(int page, int perPage, FieldFilter filter, Sort sort)
    {
        Paginator<Member> paginator = new Paginator<>(getModelClass());

        Query query;
        if (filter != null)
        {
            query = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from " + getModelClass().getSimpleName() + " mmbr left join fetch mmbr.role left join fetch mmbr.avatar left join fetch mmbr.club where " + filter.createQueryWherePart("mmbr.") + (sort != null ? (sort.createSortQueryPart(getModelClass())) : ""));
            filter.bindParams(query, getModelClass());
        }
        else if (sort != null)
        {
            query = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from " + getModelClass().getSimpleName() + " mmbr left join fetch mmbr.role left join fetch mmbr.avatar left join fetch mmbr.club " + sort.createSortQueryPart(getModelClass()));
        }
        else
        {
            query = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from " + getModelClass().getSimpleName() + " mmbr left join fetch mmbr.role left join fetch mmbr.avatar left join fetch mmbr.club ");
        }

        Paginator.Page<Member> _page = paginator.nextPage(page, perPage, query);

        ActivityManager.getInstance().add(request, null, "GET /" + getModelClass().getSimpleName().toLowerCase());

        MyResponse response = new MyResponse();
        response.setData(_page);
        return response.toResponse();
    }

    @GET
    @Path("/trainers")
    @ApiOperation(value = "List trainers for club")
    @Transactional
    public Response trainers(@QueryParam("page") int page, @QueryParam("per_page") int perPage, @QueryParam("sort") Sort sort)
    {
        Paginator<Member> paginator = new Paginator<>(getModelClass());

        OAuthAccessToken token = (OAuthAccessToken) request.getAttribute(MyResourceInterceptor.ATTR_TOKEN);

        Query query = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from " + getModelClass().getSimpleName() + " mmbr left join fetch mmbr.role left join fetch mmbr.avatar left join fetch mmbr.club where mmbr.club = :club and (mmbr.role.key = 'trainer_club_basic' or mmbr.role.key = 'parent_home_trainer') " + (sort == null ? "" : sort.createSortQueryPart(getModelClass())));
        query.setParameter("club", token.getMember().getClub());
        Paginator.Page<Member> _page = paginator.nextPage(page, perPage, query);

        ActivityManager.getInstance().add(request, null, "GET /" + getModelClass().getSimpleName().toLowerCase());

        MyResponse response = new MyResponse();
        response.setData(_page);
        return response.toResponse();
    }

    @Nonnull
    @Override
    protected Class<Member> getModelClass()
    {
        return Member.class;
    }
}