package com.newtecsolutions.floorball.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.newtecsolutions.floorball.utils.HibernateUtil;

import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Created by pedja on 6/20/17 2:33 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Role extends HibernateModel
{
    public enum FilterType
    {
        or, and
    }

    private String name;
    private String key;
    private String permissions;
    private String description;
    @JsonIgnore
    private List<Permission> permissionsCache;

    @Column(nullable = false)
    public String getPermissions()
    {
        return permissions;
    }

    public void setPermissions(String permissions)
    {
        permissionsCache = null;
        this.permissions = permissions;
    }

    @Column(name = "\"key\"", nullable = false)
    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    @Column(nullable = false)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
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

    /**
     * Get roles filtered by permissions
     * @param filterPermissions permissions to filter roles by
     * @param filterType type of filter. {@link FilterType#and} or {@link FilterType#or}*/
    public static List<Role> getRoles(Set<Permission> filterPermissions, FilterType filterType)
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        List<Role> roles = session.createQuery("from Role").list();
        if(filterPermissions == null || filterPermissions.isEmpty() || filterType == null)
        {
            return roles;
        }
        ArrayList<Role> keep = new ArrayList<>(roles.size());
        for(Role role : roles)
        {
            if(filterType == FilterType.or && !Collections.disjoint(role.getPermissionsList(), filterPermissions))
            {
                keep.add(role);
            }
            else if(filterType == FilterType.and && role.getPermissionsList().containsAll(filterPermissions))
            {
                keep.add(role);
            }
        }
        return keep;
    }

    /**
     * Filter roles using {@link FilterType#and}
     * @see #getRoles(Set, FilterType)  */
    public static List<Role> findRolesWithPermission(Permission permission)
    {
        return getRoles(Collections.singleton(permission), FilterType.and);
    }
}
