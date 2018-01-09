package com.newtecsolutions.floorball.fcm.xmpp.service.impl;


import com.newtecsolutions.floorball.fcm.xmpp.bean.CcsInMessage;
import com.newtecsolutions.floorball.fcm.xmpp.bean.CcsOutMessage;
import com.newtecsolutions.floorball.fcm.xmpp.server.CcsClient;
import com.newtecsolutions.floorball.fcm.xmpp.server.MessageHelper;
import com.newtecsolutions.floorball.fcm.xmpp.service.PayloadProcessor;
import com.newtecsolutions.floorball.fcm.xmpp.util.Util;

/**
 * Handles an upstream message request
 */
public class MessageProcessor implements PayloadProcessor
{

	@Override
	public void handleMessage(CcsInMessage inMessage) {
		CcsClient client = CcsClient.getInstance();
		String messageId = Util.getUniqueMessageId();
		String to = inMessage.getDataPayload().get(Util.PAYLOAD_ATTRIBUTE_RECIPIENT);

		// TODO: handle the data payload sent to the client device. Here, I just
		// resend the incoming message.
		CcsOutMessage outMessage = new CcsOutMessage(to, messageId, inMessage.getDataPayload());
		String jsonRequest = MessageHelper.createJsonOutMessage(outMessage);
		client.send(jsonRequest);
	}

}