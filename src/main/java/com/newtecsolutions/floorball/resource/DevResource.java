package com.newtecsolutions.floorball.resource;


import com.newtecsolutions.floorball.FBException;
import com.newtecsolutions.floorball.MyResponse;
import com.newtecsolutions.floorball.mail.PostOffice;
import com.newtecsolutions.floorball.model.Mail;
import com.newtecsolutions.floorball.utils.ConfigManager;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Created by pedja on 8/26/17.
 */

@Path("/dev")
@Produces({"application/json"})
public class DevResource
{
    private static final String TOKEN = "joWSmATygo99fpPAiy74vpXWQVw6LwgBgzI9At9dwtDz7J7tPrZOsGL4bsueMvV1uQaslAtTHX1sOcWWnMOcoKBgEIxBL8Azi4y0";

    @GET
    @Path("/mail/{token}")
    public Response mail(@PathParam("token") String token)
    {
        checkToken(token);
        MyResponse response = new MyResponse();
        response.setData(PostOffice.getInstance().info());

        return response.toResponse();
    }

    @GET
    @Path("/log/{token}")
    public String log(@PathParam("token") String token)
    {
        checkToken(token);
        return getLastNLines(5000, "/opt/photometo/logs/catalina.2017-08-27.log");
    }

    @GET
    @Path("/config/{token}")
    public String config(@PathParam("token") String token)
    {
        checkToken(token);
        return getLastNLines(5000, "/opt/photometo/.photometo/photometo.conf");
    }

    @POST
    @Path("/config/{token}")
    public Response writeConfig(@PathParam("token") String token, @QueryParam("config") String config)
    {
        checkToken(token);

        try
        {
            FileUtils.writeStringToFile(new File("/opt/photometo/.photometo/photometo.conf"), config);
        }
        catch (IOException e)
        {
            return MyResponse.errorResponse(MyResponse.ErrorCode.server_error);
        }

        return new MyResponse().toResponse();
    }

    @POST
    @Path("/mail/send/{token}")
    public Response sendMail(@PathParam("token") String token, @FormParam("to") String to, @FormParam("message") String message, @FormParam("subject") String subject)
    {
        checkToken(token);

        Mail mail = new Mail();
        mail.setFrom(ConfigManager.getInstance().getString(ConfigManager.CONFIG_MAIL_FROM, "noreply@localhost"));
        mail.setMessage(message);
        mail.setSubject(subject);
        mail.setTo(to);

        boolean sent = PostOffice.getInstance().sendMailDirectly(mail);

        return new MyResponse().setData(sent).toResponse();
    }

    private void checkToken(String token)
    {
        if (!TOKEN.equals(token))
            throw new FBException("Invalid token");
    }

    @GET
    @Path("/config")
    public String log()
    {
        return getLastNLines(5000, "/opt/photometo/logs/catalina.2017-08-27.log");
    }

    private String getLastNLines(int lines, String file)
    {
        StringBuilder s = new StringBuilder();
        try
        {
            Process p = Runtime.getRuntime().exec("tail -" + lines + " " + file);
            java.io.BufferedReader input = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
            String line;
            //Here we first read the next line into the variable
            //line and then check for the EOF condition, which
            //is the return value of null
            while ((line = input.readLine()) != null)
            {
                s.append(line).append('\n');
            }
        }
        catch (IOException e)
        {
            return e.getMessage();
        }
        return s.toString();
    }
}
