package com.newtecsolutions.floorball.fcm.xmpp.service.impl;


import com.newtecsolutions.floorball.fcm.xmpp.bean.CcsInMessage;
import com.newtecsolutions.floorball.fcm.xmpp.bean.CcsOutMessage;
import com.newtecsolutions.floorball.fcm.xmpp.server.CcsClient;
import com.newtecsolutions.floorball.fcm.xmpp.server.MessageHelper;
import com.newtecsolutions.floorball.fcm.xmpp.service.PayloadProcessor;
import com.newtecsolutions.floorball.fcm.xmpp.util.Util;

/**
 * Handles an echo request
 */
public class EchoProcessor implements PayloadProcessor
{

    @Override
    public void handleMessage(CcsInMessage inMessage)
    {
        CcsClient client = CcsClient.getInstance();
        String messageId = Util.getUniqueMessageId();
        String to = inMessage.getFrom();

        // Send the incoming message to the the device that made the request
        CcsOutMessage outMessage = new CcsOutMessage(to, messageId, inMessage.getDataPayload());
        String jsonRequest = MessageHelper.createJsonOutMessage(outMessage);
        client.send(jsonRequest);
    }

}