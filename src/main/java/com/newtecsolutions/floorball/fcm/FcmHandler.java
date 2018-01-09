package com.newtecsolutions.floorball.fcm;

import com.newtecsolutions.floorball.fcm.http.FcmHttpSender;
import com.newtecsolutions.floorball.fcm.xmpp.server.CcsClient;
import com.newtecsolutions.floorball.model.Notification;
import com.newtecsolutions.floorball.utils.ConfigManager;
import com.newtecsolutions.floorball.utils.LogUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by pedja on 9/18/16.
 */

public class FcmHandler
{
    private static FcmHandler instance;
    private FcmSender sender;

    public static FcmHandler getInstance()
    {
        if(instance == null)
        {
            instance = new FcmHandler();
        }
        return instance;
    }

    private FcmHandler()
    {
        //initialize xmpp for sending notifications, or fallback to http
        try
        {
            CcsClient.init(ConfigManager.getInstance().getString(ConfigManager.CONFIG_FCM_PROJECT_ID),
                    ConfigManager.getInstance().getString(ConfigManager.CONFIG_FCM_API_KEY),
                    ConfigManager.getInstance().getBoolean(ConfigManager.CONFIG_FCM_DEBUG, true));
            CcsClient.getInstance().connect();
            sender = CcsClient.getInstance();
        }
        catch (XMPPException | SmackException | IOException | InterruptedException e)
        {
            LogUtils.getLogger().log(Level.WARNING, e.getMessage(), e);
            sender = new FcmHttpSender();
        }
    }

    public void send(Notification notification, List<String> registrationIds)
    {
        sender.send(notification, registrationIds);
    }

    public void shutdown()
    {
        if(sender != null)
        {
            sender.shutdown();
        }
    }
}
