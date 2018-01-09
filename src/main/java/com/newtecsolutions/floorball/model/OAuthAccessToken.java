package com.newtecsolutions.floorball.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "member"})
public class OAuthAccessToken
{
    public static final long DEFAULT_EXPIRES_MILLIS = 24 * 60 * 60 * 1000;
    private String accessToken, scope;
    private Member member;
    private Date expires;
    private OAuthClient client;

    @Id
    @Column(length = 64)
    public String getAccessToken()
    {
        return accessToken;
    }

    public void setAccessToken(String accessToken)
    {
        this.accessToken = accessToken;
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

    @ManyToOne(cascade= CascadeType.ALL, fetch=FetchType.EAGER)
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
}
