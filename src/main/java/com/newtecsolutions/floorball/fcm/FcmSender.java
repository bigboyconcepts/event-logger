package com.newtecsolutions.floorball.fcm;

import com.newtecsolutions.floorball.model.Notification;

import java.util.List;

/**
 * Created by pedja on 7/14/17 9:46 AM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public interface FcmSender
{
    /**
     * Send notification
     * @param notification Notification to send
     * @param registrationIds recipients for notification*/
    void send(Notification notification, List<String> registrationIds);

    /**
     * Shutdown sender*/
    void shutdown();
}
