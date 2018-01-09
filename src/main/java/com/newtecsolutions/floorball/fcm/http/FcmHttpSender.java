package com.newtecsolutions.floorball.fcm.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.newtecsolutions.floorball.fcm.FcmSender;
import com.newtecsolutions.floorball.model.Notification;
import com.newtecsolutions.floorball.utils.ConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by pedja on 7/14/17 9:43 AM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class FcmHttpSender implements FcmSender
{
    private static final String FCM_SEND_URL = "https://fcm.googleapis.com/fcm/send";

    private BlockingQueue<Message> mNotificationQueue;
    private List<Worker> mWorkers;

    public FcmHttpSender()
    {
        //initialize notification queue and workers
        mNotificationQueue = new LinkedBlockingQueue<>();
        int workerThreadCount = ConfigManager.getInstance().getInt(ConfigManager.CONFIG_MAIL_WORKER_THREAD_COUNT, 2);
        mWorkers = new ArrayList<>(workerThreadCount);
        for (int i = 0; i < workerThreadCount; i++)
        {
            Worker worker = new Worker(this);
            mWorkers.add(worker);
            worker.start();
        }
    }

    @Override
    public void shutdown()
    {
        for (Worker worker : mWorkers)
        {
            worker.quit();
        }
        mWorkers.clear();
    }

    @Override
    public void send(Notification notification, List<String> registrationIds)
    {
        mNotificationQueue.add(new Message(notification, registrationIds));
    }

    private static class Worker extends Thread
    {
        private boolean mQuit;

        private FcmHttpSender sender;
        private ObjectMapper mapper;
        private Client client;
        private WebTarget webTarget;

        Worker(FcmHttpSender sender)
        {
            this.sender = sender;
            mapper = new ObjectMapper();
            client = ClientBuilder.newClient();
            webTarget = client.target(FCM_SEND_URL);
        }

        @Override
        public void run()
        {
            while (true)
            {
                final Message message;
                try
                {
                    // Take a request from the queue.
                    message = sender.mNotificationQueue.take();
                }
                catch (InterruptedException e)
                {
                    // We may have been interrupted because it was time to quit.
                    if (mQuit)
                    {
                        return;
                    }
                    continue;
                }

                //split recipients to chunks of max 1000, since that is max for fcm http
                int currentIndex = 0, listIndex = 0;
                List<String> regIdStrings = new ArrayList<>(1000);
                while(listIndex < message.registrationIds.size())
                {
                    if(currentIndex == 999)
                    {
                        send(message.notification, regIdStrings);
                        currentIndex = 0;
                        regIdStrings.clear();
                    }
                    regIdStrings.add(regIdStrings.get(listIndex));
                    currentIndex++;
                    listIndex++;
                }
                if(!regIdStrings.isEmpty())
                {
                    send(message.notification, regIdStrings);
                }
            }
        }

        private void send(Notification notification, List<String> regIdStrings)
        {
            ObjectNode rootNode = mapper.createObjectNode();
            ArrayNode rids = mapper.createArrayNode();
            for(String regId : regIdStrings)
            {
                rids.add(regId);
            }
            rootNode.set("registration_ids", rids);
            ObjectNode notif = mapper.createObjectNode();
            notif.put("title", notification.getTitle());
            notif.put("body", notification.getText());
            notif.put("click_action", "notification");
            rootNode.set("notification", notif);

            ObjectNode data = mapper.createObjectNode();
            data.put("notification", mapper.valueToTree(notification));
            rootNode.set("data", data);

            Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
            invocationBuilder.header("Authorization", "key=" + ConfigManager.getInstance().getString(ConfigManager.CONFIG_FCM_API_KEY));
            Response response = invocationBuilder.post(Entity.entity(rootNode.toString(), MediaType.APPLICATION_JSON));
            //TODO log success/fail
        }

        void quit()
        {
            mQuit = false;
            interrupt();
            //TODO don't interrupt, or persist messages, messages wont be send, messages aren't persisted.
        }
    }

    private class Message
    {
        private final Notification notification;
        private final List<String> registrationIds;

        Message(Notification notification, List<String> registrationIds)
        {
            this.notification = notification;
            this.registrationIds = registrationIds;
        }
    }
}
