package com.newtecsolutions.floorball.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletContext;

import freemarker.cache.WebappTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Created by pedja on 19.9.16. 14.28.
 * This class is part of the Floorball
 * Copyright © 2016 ${OWNER}
 *
 * Freemarker template manager
 */

public class TemplateManager
{
    private static TemplateManager instance;

    public static TemplateManager getInstance()
    {
        if(instance == null)
            throw new IllegalStateException("TemplateManager is not initialized. call init(ServletContext) first");
        return instance;
    }

    public static void init(ServletContext context)
    {
        instance = new TemplateManager(context);
    }

    private Configuration cfg;

    public TemplateManager(ServletContext context)
    {
        cfg = new Configuration();
        WebappTemplateLoader templateLoader = new WebappTemplateLoader(context, "freemarker");
        templateLoader.setURLConnectionUsesCaches(false);
        templateLoader.setAttemptFileAccess(false);
        cfg.setTemplateLoader(templateLoader);
    }

    /**
     * */
    public String processTemplate(String templateFile, Map<String, Object> map)
    {
        try
        {
            Template template = cfg.getTemplate(templateFile);
            StringWriter sw = new StringWriter();
            template.process(map, sw);
            return sw.toString();
        }
        catch (IOException | TemplateException e)
        {
            LogUtils.getLogger().log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
