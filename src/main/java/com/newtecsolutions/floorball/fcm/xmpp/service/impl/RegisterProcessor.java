package com.newtecsolutions.floorball.fcm.xmpp.service.impl;


import com.newtecsolutions.floorball.fcm.xmpp.bean.CcsInMessage;
import com.newtecsolutions.floorball.fcm.xmpp.service.PayloadProcessor;

/**
 * Handles a user registration request
 */
public class RegisterProcessor implements PayloadProcessor
{

    @Override
    public void handleMessage(CcsInMessage msg)
    {
        // TODO: handle the user registration. Keep in mind that a user name can
        // have more reg IDs associated. The messages IDs should be uniques.
    }

}