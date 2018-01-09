package com.newtecsolutions.floorball.model;

import com.newtecsolutions.floorball.utils.HibernateUtil;

import org.hibernate.Session;
import org.skynetsoftware.jutils.StringUtils;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * Created by pedja on 9/17/16.
 */

@Entity
public class OAuthClient
{
    private String clientId, clientSecret, redirectUri, grantTypes, scope;
    private List<OAuthAccessToken> accessTokens;
    private List<OAuthRefreshToken> refreshTokens;

    @Id
    @Column(length = 64)
    public String getClientId()
    {
        return clientId;
    }

    public void setClientId(String clientId)
    {
        this.clientId = clientId;
    }

    @Column(nullable = false, length = 64)
    public String getClientSecret()
    {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret)
    {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUri()
    {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri)
    {
        this.redirectUri = redirectUri;
    }

    @Column(nullable = false)
    public String getGrantTypes()
    {
        return grantTypes;
    }

    public void setGrantTypes(String grantTypes)
    {
        this.grantTypes = grantTypes;
    }

    @Column(nullable = false)
    public String getScope()
    {
        return scope;
    }

    public void setScope(String scope)
    {
        this.scope = scope;
    }

    @OneToMany(cascade= CascadeType.ALL, mappedBy="client")
    public List<OAuthAccessToken> getAccessTokens()
    {
        return accessTokens;
    }

    public void setAccessTokens(List<OAuthAccessToken> accessTokens)
    {
        this.accessTokens = accessTokens;
    }

    @OneToMany(cascade= CascadeType.ALL, mappedBy="client")
    public List<OAuthRefreshToken> getRefreshTokens()
    {
        return refreshTokens;
    }

    public void setRefreshTokens(List<OAuthRefreshToken> refreshTokens)
    {
        this.refreshTokens = refreshTokens;
    }

    /**
     * Get OAuthClient*/
    public static OAuthClient getClient(String clientId, String clientSecret)
    {
        if (StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret))
            return null;
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        OAuthClient client = session.get(OAuthClient.class, clientId);
        return client == null ? null : clientSecret.equals(client.getClientSecret()) ? client : null;
    }
}
