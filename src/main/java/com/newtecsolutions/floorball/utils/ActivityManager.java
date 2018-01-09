package com.newtecsolutions.floorball.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newtecsolutions.floorball.model.Member;
import com.newtecsolutions.floorball.socketio.SocketIOServerManager;

import java.util.LinkedList;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by pedja on 1/10/17 2:25 PM.
 * This class is part of the Floorball
 * Copyright © 2017 ${OWNER}
 */

public class ActivityManager
{
    public static final String TOKEN = "Sqn2wLQJd2dEEqcEZacQ1LpJBJD8hG0wBeMp9t2n957eym9FKzznLUhOcjkRbosqRxlgXpiiC2h9aix6euRpNIXZoNRs62Sr6Giygp31Iz3jT3L4LXwPoS8Z";
    private static ActivityManager instance = new ActivityManager();

    public static ActivityManager getInstance()
    {
        return instance;
    }

    private final LinkedList<Activity> activities = new LinkedList<>();
    private final int maxEntries = ConfigManager.getInstance().getInt(ConfigManager.CONFIG_AM_MAX_ENTRIES, 100);

    /**
     * Log activity
     * @param action for example /member/1*/
    public synchronized void add(HttpServletRequest request, Member member, String action)
    {
        if(request == null)
            return;
        if(activities.size() >= maxEntries)
        {
            activities.removeLast();
        }
        Activity activity = new Activity();
        activity.setRemoteAddress(request.getRemoteAddr());
        activity.setAction(action);
        activity.setTime(System.currentTimeMillis());
        if (member != null)
        {
            activity.setMemberUsername(member.getEmail());
            activity.setMemberId(member.getId());
        }
        activity.setId(activities.size());
        activities.add(activity);
        SocketIOServerManager.getInstance().sendEvent("activity", activity.toJSON());
    }

    public LinkedList<Activity> getActivities()
    {
        return activities;
    }

    public static class Activity implements Comparable<Activity>
    {
        private int id;
        private String remoteAddress;
        private String action;
        private long time;
        private String memberUsername;
        private long memberId;

        public int getId()
        {
            return id;
        }

        public void setId(int id)
        {
            this.id = id;
        }

        public String getRemoteAddress()
        {
            return remoteAddress;
        }

        private void setRemoteAddress(String remoteAddress)
        {
            this.remoteAddress = remoteAddress;
        }

        public String getAction()
        {
            return action;
        }

        private void setAction(String action)
        {
            this.action = action;
        }

        public long getTime()
        {
            return time;
        }

        private void setTime(long lastActionTime)
        {
            this.time = lastActionTime;
        }

        public String getMemberUsername()
        {
            return memberUsername;
        }

        private void setMemberUsername(String memberUsername)
        {
            this.memberUsername = memberUsername;
        }

        public long getMemberId()
        {
            return memberId;
        }

        private void setMemberId(long memberId)
        {
            this.memberId = memberId;
        }

        String toJSON()
        {
            ObjectMapper mapper = new ObjectMapper();
            try
            {
                return mapper.writeValueAsString(this);
            }
            catch (JsonProcessingException e)
            {
                LogUtils.getLogger().log(Level.FINE, e.getMessage(), e);
                return null;
            }
        }

        @Override
        public int compareTo(Activity activity)
        {
            return Long.compare(this.time, activity.time);
        }
    }
}
