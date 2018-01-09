package com.newtecsolutions.floorball;

/**
 * Created by pedja on 7/4/17 2:21 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class DevelopmentServer
{
    //used for development to easily start server, don't include in jar manifest, it is not used as app starting point in production
    public static void main(String[] args) throws Exception
    {
        GrizzlyDaemon server = new GrizzlyDaemon();
        server.init(null);
        server.start();
    }
}
