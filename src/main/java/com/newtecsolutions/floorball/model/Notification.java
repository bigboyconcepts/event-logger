package com.newtecsolutions.floorball.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * Created by pedja on 7/4/17 8:15 AM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
@Entity
public class Notification extends HibernateModel
{
    private String title, text;
    private File image;

    @Column(nullable = false)
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    @Column(nullable = false)
    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    @OneToOne()
    public File getImage()
    {
        return image;
    }

    public void setImage(File image)
    {
        this.image = image;
    }
}
