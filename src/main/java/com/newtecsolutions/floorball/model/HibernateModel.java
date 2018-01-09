package com.newtecsolutions.floorball.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Created by pedja on 16.9.16. 08.21.
 * This class is part of the Floorball
 * Copyright © 2016 ${OWNER}
 */

@MappedSuperclass
public class HibernateModel
{
    private long id;
    private Date created, modified;
    private boolean active = true;

    public HibernateModel()
    {
        created = new Date();
        modified = created;
    }

    @Id
    @GeneratedValue()
    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Date getCreated()
    {
        return created;
    }

    public void setCreated(Date created)
    {
        this.created = created;
    }

    public Date getModified()
    {
        return modified;
    }

    public void setModified(Date modified)
    {
        this.modified = modified;
    }

    @Column()
    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }
}
