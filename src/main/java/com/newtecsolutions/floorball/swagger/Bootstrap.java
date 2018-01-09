package com.newtecsolutions.floorball.swagger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.Swagger;

public class Bootstrap extends HttpServlet
{
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        Info info = new Info()
                .title("Floorball REST API")
                .description("REST API for Floorball")
                //.termsOfService("http://swagger.io/terms/")
                .contact(new Contact()
                        .email("predragcokulov@gmail.com"))
                /*.license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"))*/;

        ServletContext context = config.getServletContext();
        Swagger swagger = new Swagger().info(info);
        //swagger.securityDefinition("access_token", new ApiKeyAuthDefinition("Authorization", In.HEADER));
        new SwaggerContextService().withServletConfig(config).updateSwagger(swagger);
    }
}