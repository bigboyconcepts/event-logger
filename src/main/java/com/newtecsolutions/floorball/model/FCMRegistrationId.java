package com.newtecsolutions.floorball.model;

import com.newtecsolutions.floorball.utils.HibernateUtil;

import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Created by pedja on 7/4/17 8:05 AM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 *
 * Cases:
 *
 * 1 device - 1 token
 * 1 device - n members
 * 1 token - 1 device
 * 1 token - 1 member
 * 1 member - 1 token
 * 1 member - n devices
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"deviceId", "member_id"}))
public class FCMRegistrationId extends HibernateModel
{
    private enum Target
    {
        android, ios
    }
    private String deviceId;
    private String fcmRegistrationId;
    private Member member;
    private Target target;

    @Column(nullable = false)
    public String getDeviceId()
    {
        return deviceId;
    }

    public void setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;
    }

    @Column(nullable = false)
    public String getFcmRegistrationId()
    {
        return fcmRegistrationId;
    }

    public void setFcmRegistrationId(String fcmRegistrationId)
    {
        this.fcmRegistrationId = fcmRegistrationId;
    }

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public Member getMember()
    {
        return member;
    }

    public void setMember(Member member)
    {
        this.member = member;
    }

    @Enumerated(EnumType.STRING)
    public Target getTarget()
    {
        return target;
    }

    public void setTarget(Target target)
    {
        this.target = target;
    }

    /**
     * Find registrationId with member and registrationId*/
    public static FCMRegistrationId findRegistrationId(Member member, String registrationId)
    {
        Query query = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from FMCRegistrationId where 'member' = :mmbr and registrationId = :rid");
        query.setParameter("rid", registrationId);
        query.setParameter("mmbr", member);
        return (FCMRegistrationId) query.getSingleResult();
    }

    /**
     * Get all registration ids as string list*/
    public static List<String> getRegistrationIdsAsStringList()
    {
        List<FCMRegistrationId> list = HibernateUtil.getSessionFactory().getCurrentSession().createQuery("from FCMRegistrationId").getResultList();
        List<String> ids = new ArrayList<>(list.size());
        for(FCMRegistrationId id : list)
            ids.add(id.getFcmRegistrationId());
        return ids;
    }
}
