package com.newtecsolutions.floorball;


import com.newtecsolutions.floorball.fcm.FcmHandler;
import com.newtecsolutions.floorball.mail.PostOffice;
import com.newtecsolutions.floorball.socketio.SocketIOServerManager;
import com.newtecsolutions.floorball.utils.HibernateUtil;
import com.newtecsolutions.floorball.utils.TemplateManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


public class ServletContextClass implements ServletContextListener
{
    @Override
    public void contextInitialized(ServletContextEvent event)
    {
        OpenCVJNI.init();//load opencv native library
        HibernateUtil.getSessionFactory();// Just call the static initializer of that class
        TemplateManager.init(event.getServletContext());//init freemarker
        SocketIOServerManager.init();//start socket io server. Right now used only for logging api call activity
        FcmHandler.getInstance();//initialize xmpp or fallback to http
    }

    @Override
    public void contextDestroyed(ServletContextEvent event)
    {
        // Free all resources
        HibernateUtil.getSessionFactory().close();
        PostOffice.shutdownIfRunning();
        FcmHandler.getInstance().shutdown();
        SocketIOServerManager.destroy();
    }
}