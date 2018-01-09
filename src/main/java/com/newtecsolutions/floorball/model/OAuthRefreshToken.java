package com.newtecsolutions.floorball.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.newtecsolutions.floorball.utils.HibernateUtil;

import org.hibernate.Session;
import org.skynetsoftware.jutils.StringUtils;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Created by pedja on 9/18/16.
 */

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "member", "client"})
public class OAuthRefreshToken
{
    public static final long DEFAULT_EXPIRES_MILLIS = 7 * 24 * 60 * 60 * 1000;
    private String refreshToken, scope;
    private Member member;
    private Date expires;
    private OAuthClient client;

    @Id
    @Column(length = 64)
    public String getRefreshToken()
    {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken)
    {
        this.refreshToken = refreshToken;
    }

    @ManyToOne(cascade= CascadeType.ALL, fetch= FetchType.LAZY)
    public OAuthClient getClient()
    {
        return client;
    }

    public void setClient(OAuthClient client)
    {
        this.client = client;
    }

    public String getScope()
    {
        return scope;
    }

    public void setScope(String scope)
    {
        this.scope = scope;
    }

    @ManyToOne(cascade= CascadeType.ALL, fetch=FetchType.LAZY)
    public Member getMember()
    {
        return member;
    }

    public void setMember(Member member)
    {
        this.member = member;
    }

    @Column(nullable = false)
    public Date getExpires()
    {
        return expires;
    }

    public void setExpires(Date expires)
    {
        this.expires = expires;
    }

    /**
     * Find OAuthRefreshToken*/
    public static OAuthRefreshToken getRefreshToken(String refreshToken)
    {
        if (StringUtils.isEmpty(refreshToken))
            return null;
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        return session.get(OAuthRefreshToken.class, refreshToken);
    }
}
