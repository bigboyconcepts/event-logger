package com.newtecsolutions.floorball.model;

import com.newtecsolutions.floorball.utils.HibernateUtil;
import com.newtecsolutions.floorball.utils.LocaleManager;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.hibernate.Session;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 * Created by pedja on 6/28/17 10:42 AM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
@Entity
public class Club extends HibernateModel
{
    private String name;
    private File logo;
    private Set<Member> members;

    @Column(length = 32)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    public File getLogo()
    {
        return logo;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "club")
    public Set<Member> getMembers()
    {
        return members;
    }

    public void setMembers(Set<Member> members)
    {
        this.members = members;
    }

    public void setLogo(File logo)
    {
        this.logo = logo;
    }

    /**
     * Creates club, uploads photo for club, and creates registration code for all roles*/
    public static Club createClub(String name, InputStream imageInputStream, FormDataContentDisposition imageMetaData, LocaleManager localeManager)
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Club club = new Club();
        club.setName(name);
        session.save(club);

        if (imageInputStream != null && imageMetaData != null)
        {
            File file = File.uploadFile(club, imageInputStream, imageMetaData, localeManager);
            club.setLogo(file);
        }

        List<Role> roles = Role.findRolesWithPermission(Permission.appmode_club_basic);

        long now = System.currentTimeMillis();
        Date expires = new Date(now + RegistrationCode.DEFAULT_EXPIRES);

        for(Role role : roles)
        {
            RegistrationCode registrationCode = new RegistrationCode();
            registrationCode.setExpires(expires);
            registrationCode.setClub(club);
            registrationCode.setRole(role);
            registrationCode.setCode(RegistrationCode.newRandomCode());
            session.save(registrationCode);
        }
        session.save(club);
        return club;
    }
}
