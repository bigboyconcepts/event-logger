package com.newtecsolutions.floorball.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

import javax.annotation.Nonnull;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

/**
 * Created by pedja on 6/22/17 1:33 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
@Entity
@Table(
        uniqueConstraints = {@UniqueConstraint(columnNames = {"source_id", "target_id", "permissions"})}
)
public class RolePermissionRole extends HibernateModel
{
    private Role source, target;
    private String permissions;
    @JsonIgnore
    private List<Permission> permissionsCache;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public Role getSource()
    {
        return source;
    }

    public void setSource(Role source)
    {
        this.source = source;
    }

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public Role getTarget()
    {
        return target;
    }

    public void setTarget(Role target)
    {
        this.target = target;
    }

    public String getPermissions()
    {
        return permissions;
    }

    public void setPermissions(String permission)
    {
        this.permissions = permission;
    }


    /**
     * Get app modes as List<Permission> since in db app modes are stored as csv string*/
    @Transient
    @JsonIgnore
    public synchronized List<Permission> getPermissionsList()
    {
        if(permissionsCache == null)
        {
            permissionsCache = Permission.listFromCsvString(getPermissions());
        }
        return permissionsCache;
    }

    public void setPermissionList(@Nonnull List<Permission> permissions)
    {
        setPermissions(Permission.stringFromList(permissions));
    }
}
