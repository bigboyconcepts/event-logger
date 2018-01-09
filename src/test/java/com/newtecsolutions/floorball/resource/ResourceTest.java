package com.newtecsolutions.floorball.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newtecsolutions.floorball.MyApplication;
import com.newtecsolutions.floorball.ServletContextClass;
import com.newtecsolutions.floorball.utils.ActivityManager;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by pedja on 8/8/17 8:22 AM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class ResourceTest
{
    private static JerseyTest jerseyTest;

    @BeforeClass
    public static void setUp() throws Exception
    {
        initJerseyTest();
        jerseyTest.setUp();
    }

    private static void initJerseyTest()
    {
        jerseyTest = new JerseyTest()
        {
            @Override
            protected TestContainerFactory getTestContainerFactory()
            {
                return new GrizzlyWebTestContainerFactory();
            }

            @Override
            protected DeploymentContext configureDeployment()
            {
                ResourceConfig config = new MyApplication();
                ServletContainer container = new ServletContainer(config);
                return ServletDeploymentContext.forServlet(container)
                        .initParam("javax.ws.rs.Application", MyApplication.class.getName())
                        .initParam("jersey.config.server.mvc.templateBasePath.freemarker", "freemarker")
                        .initParam("jersey.config.server.tracing.type", "ON_DEMAND")
                        .contextPath("/api")
                        .addListener(ServletContextClass.class).build();
            }
        };
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        jerseyTest.tearDown();
    }

    @Test
    public void testGetAllAppModes() throws IOException
    {
        String responseJson = jerseyTest.target("appmode").request().get(String.class);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> response = mapper.readValue(responseJson, new TypeReference<Map<String, Object>>()
        {
        });
        Assert.assertThat(response.get("errors"), CoreMatchers.instanceOf(List.class));
        Assert.assertThat(response.get("data"), CoreMatchers.instanceOf(List.class));
        Assert.assertTrue(((List) response.get("errors")).isEmpty());
        Assert.assertFalse(((List) response.get("data")).isEmpty());
    }

    @Test
    public void testActivityAdd() throws IOException
    {
        String responseJson = jerseyTest.target("activity").queryParam("token", ActivityManager.TOKEN).request().get(String.class);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> response = mapper.readValue(responseJson, new TypeReference<Map<String, Object>>()
        {
        });
        Assert.assertThat(response.get("errors"), CoreMatchers.instanceOf(List.class));
        Assert.assertTrue(((List) response.get("errors")).isEmpty());
    }
}
