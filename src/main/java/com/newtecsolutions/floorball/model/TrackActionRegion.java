package com.newtecsolutions.floorball.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * Created by pedja on 6/29/17 11:13 AM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
@Entity
public class TrackActionRegion extends HibernateModel
{
    private int x, y;
    private Track track;
    private File video;

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    public Track getTrack()
    {
        return track;
    }

    public void setTrack(Track track)
    {
        this.track = track;
    }

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public File getVideo()
    {
        return video;
    }

    public void setVideo(File video)
    {
        this.video = video;
    }

    @Override
    public String toString()
    {
        return "TrackActionRegion{" +
                "x=" + x +
                ", y=" + y +
                ", track=" + track +
                '}';
    }
}
