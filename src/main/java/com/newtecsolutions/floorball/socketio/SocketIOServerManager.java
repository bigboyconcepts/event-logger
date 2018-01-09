package com.newtecsolutions.floorball.socketio;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.newtecsolutions.floorball.utils.ConfigManager;
import com.newtecsolutions.floorball.utils.LogUtils;

/**
 * Created by pedja on 1/11/17 9:23 AM.
 * This class is part of the Floorball
 * Copyright © 2017 ${OWNER}
 */

public class SocketIOServerManager
{
    private static SocketIOServerManager instance;

    public static void init()
    {
        instance = new SocketIOServerManager();
    }

    public static void destroy()
    {
        if(instance != null)
        {
            instance._destroy();
            instance = null;
        }
    }

    public static SocketIOServerManager getInstance()
    {
        if(instance == null)
        {
            throw new IllegalStateException("SocketIOServerManager not initialized, call init() first. Make sure to call destroy to stop the server");
        }
        return instance;
    }

    private final SocketIOServer server;

    private SocketIOServerManager()
    {
        Configuration config = new Configuration();
        config.setPort(ConfigManager.getInstance().getInt(ConfigManager.CONFIG_SOCKETIO_PORT, 9092));

        server = new SocketIOServer(config);
        server.start();
        server.addConnectListener(client -> LogUtils.info("client connected: addr: " + client.getRemoteAddress().toString()));
    }

    public void sendEvent(String event, String data)
    {
        server.getBroadcastOperations().sendEvent(event, data);
    }

    private void _destroy()
    {
        server.stop();
    }
}
