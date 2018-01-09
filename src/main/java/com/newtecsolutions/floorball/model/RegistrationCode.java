package com.newtecsolutions.floorball.model;

import com.newtecsolutions.floorball.utils.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Date;
import java.util.Random;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NoResultException;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Created by pedja on 6/28/17 10:36 AM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
@Entity
@Table(
        uniqueConstraints = {@UniqueConstraint(columnNames = {"role_id", "club_id", "code"})}
)
public class RegistrationCode extends HibernateModel
{
    public static final long DEFAULT_EXPIRES = 31_556_952_000L;//one year
    private Role role;
    private Club club;
    private Date expires;
    private int code;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public Role getRole()
    {
        return role;
    }

    public void setRole(Role role)
    {
        this.role = role;
    }

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public Club getClub()
    {
        return club;
    }

    public void setClub(Club club)
    {
        this.club = club;
    }

    public Date getExpires()
    {
        return expires;
    }

    public void setExpires(Date expires)
    {
        this.expires = expires;
    }

    @Column(length = 6)
    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }


    private static final Random RAND = new Random();

    /**
     * Create new random it used as RegistrationCode. It will be 6 digits*/
    public static int newRandomCode()
    {
        return RAND.nextInt(900000) + 100000;
    }

    /**
     * Find RegistrationCode in db by 6 digit code*/
    public static RegistrationCode findByCode(int registrationCode)
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Query query = session.createQuery("from RegistrationCode where code = :code");
        query.setParameter("code", registrationCode);
        try
        {
            return (RegistrationCode) query.getSingleResult();
        }
        catch (NoResultException e)
        {
            return null;
        }
    }
}
