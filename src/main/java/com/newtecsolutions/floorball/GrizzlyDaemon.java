package com.newtecsolutions.floorball;


import com.newtecsolutions.floorball.swagger.Bootstrap;
import com.newtecsolutions.floorball.utils.ConfigManager;
import com.newtecsolutions.floorball.utils.LogUtils;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.servlet.ServletContainer;
import org.skynetsoftware.jutils.StringUtils;

import java.io.File;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import io.swagger.jersey.config.JerseyJaxrsConfig;
//createing keystore for ssl
//http://xacmlinfo.org/2014/06/13/how-to-keystore-creating-jks-file-from-existing-private-key-and-certificate/
public class GrizzlyDaemon implements Daemon
{
    /**
     * Grizzly HttpServer instance*/
    private HttpServer server;

    static
    {
        //init static content dir, used for dynamic(uploaded) files
        File filesFolder = new File(ConfigManager.getInstance().getString(ConfigManager.CONFIG_SERVER_STATIC_CONTENT_DIR, ""));
        LogUtils.info("Static content dir: " + filesFolder);
        if(!filesFolder.exists())
        {
            throw new RuntimeException(String.format("%s points to a missing dir.", ConfigManager.CONFIG_SERVER_STATIC_CONTENT_DIR));
        }
    }

    @Override
    public void init(DaemonContext context) throws DaemonInitException
    {
        String sslKeystorePath = ConfigManager.getInstance().getString(ConfigManager.CONFIG_SSL_KEYSTORE_PATH, null);
        String sslKeystorePassword = ConfigManager.getInstance().getString(ConfigManager.CONFIG_SSL_KEYSTORE_PASSWORD, null);

        boolean useSsl = ConfigManager.getInstance().getBoolean(ConfigManager.CONFIG_SSL_ENABLE, false)
                && !StringUtils.isEmpty(sslKeystorePassword) && !StringUtils.isEmpty(sslKeystorePath);

        //Base server url with port. Server url must be 0.0.0.0 to allow external traffic, port is read from config, dont hardcode it
        final URI BASE_URI = UriBuilder.fromUri((useSsl ? "https" : "http") + ("://0.0.0.0/")).port(ConfigManager.getInstance().getInt(ConfigManager.CONFIG_SERVER_PORT, 8080)).build();

        // Create HttpServer
        final HttpServer serverLocal;

        if(useSsl)
        {
            SSLContextConfigurator sslContext = new SSLContextConfigurator();
            sslContext.setKeyStoreFile(sslKeystorePath);
            sslContext.setKeyStorePass(sslKeystorePassword);

            serverLocal = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, (GrizzlyHttpContainer)null, true, new SSLEngineConfigurator(sslContext, false, false, false), false);
        }
        else
        {
            serverLocal = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, false);
        }

        final WebappContext webappContext = new WebappContext("Floorball REST API", "");

        webappContext.addListener(ServletContextClass.class);

        // Initialize and register Jersey ServletContainer
        ServletRegistration servletRegistration = webappContext.addServlet(MyApplication.class.getName(), ServletContainer.class);
        servletRegistration.addMapping("/api/*");
        servletRegistration.setInitParameter("javax.ws.rs.Application", MyApplication.class.getName());
        servletRegistration.setInitParameter("jersey.config.server.mvc.templateBasePath.freemarker", "freemarker");
        servletRegistration.setInitParameter("jersey.config.server.tracing.type", "ON_DEMAND");
        servletRegistration.setLoadOnStartup(1);

        // Initialize and register Swagger Jersey2Config
        ServletRegistration swaggerServletRegistration = webappContext.addServlet("Jersey2Config", JerseyJaxrsConfig.class);
        swaggerServletRegistration.addMapping("/sw/*");
        swaggerServletRegistration.setInitParameter("api.version", "1.0.0");
        swaggerServletRegistration.setInitParameter("swagger.api.basepath", "/api");
        swaggerServletRegistration.setInitParameter("swagger.api.schemes", "http, https");
        swaggerServletRegistration.setLoadOnStartup(2);

        // Initialize and register Swagger Bootstrap
        ServletRegistration swaggerBootstrapServletRegistration = webappContext.addServlet("Bootstrap", Bootstrap.class);
        swaggerBootstrapServletRegistration.addMapping("/sw/*");
        swaggerBootstrapServletRegistration.setLoadOnStartup(2);

        //static content handler for uploaded files
        StaticHttpHandler filesHttpHandler = new StaticHttpHandler(ConfigManager.getInstance().getString(ConfigManager.CONFIG_SERVER_STATIC_CONTENT_DIR, ""));
        serverLocal.getServerConfiguration().addHttpHandler(filesHttpHandler, "/files");

        //static content handler for admin panel
        CLStaticHttpHandler staticHttpHandler = new CLStaticHttpHandler(MyApplication.class.getClassLoader(), "admin/", "/");
        serverLocal.getServerConfiguration().addHttpHandler(staticHttpHandler, "/admin");

        //static content handler for swagger
        CLStaticHttpHandler swaggerHttpHandler = new CLStaticHttpHandler(MyApplication.class.getClassLoader(), "swagger/");
        serverLocal.getServerConfiguration().addHttpHandler(swaggerHttpHandler, "/swagger");

        //static content handler for front
        CLStaticHttpHandler frontHttpHandler = new CLStaticHttpHandler(MyApplication.class.getClassLoader(), "front/");
        serverLocal.getServerConfiguration().addHttpHandler(frontHttpHandler);

        webappContext.deploy(serverLocal);

        server = serverLocal;
    }

    @Override
    public void start() throws Exception
    {
        if(!server.isStarted())server.start();
    }

    @Override
    public void stop() throws Exception
    {
        if(server.isStarted())server.shutdownNow();
    }

    @Override
    public void destroy()
    {
        server = null;
    }


}