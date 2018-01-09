package com.newtecsolutions.floorball.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.newtecsolutions.floorball.utils.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

/**
 * Created by pedja on 6/28/17 1:27 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
@Entity
public class Track extends HibernateModel
{
    private String name;
    private File trackImage;
    private Set<File> trackVideos;
    private String appModes;
    private Set<TrackActionRegion> actionRegions;

    @JsonIgnore
    private List<AppMode> modesCache;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    public File getTrackImage()
    {
        return trackImage;
    }

    public void setTrackImage(File trackImage)
    {
        this.trackImage = trackImage;
    }

    /**
     * Since hibernate will auto-create join table, it is not possible to set constraint ON_DELETE to CASCADE for track_id and NO ACTION for file_id, need to set it manually in mysql*/
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinTable(name = "TrackVideo", joinColumns = { @JoinColumn(name = "track_id") }, inverseJoinColumns = { @JoinColumn(name = "file_id") })
    public Set<File> getTrackVideos()
    {
        return trackVideos;
    }

    public void setTrackVideos(Set<File> trackVideos)
    {
        this.trackVideos = trackVideos;
    }

    @Column(nullable = false)
    public void setAppModes(String appModes)
    {
        this.appModes = appModes;
    }

    public String getAppModes()
    {
        return appModes;
    }

    @OneToMany(fetch = FetchType.LAZY, cascade= CascadeType.ALL, mappedBy="track", orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Set<TrackActionRegion> getActionRegions()
    {
        return actionRegions;
    }

    public void setActionRegions(Set<TrackActionRegion> actionRegions)
    {
        this.actionRegions = actionRegions;
    }

    /**
     * Get app modes as List<AppMode> since in db app modes are stored as csv string*/
    @Transient
    @JsonIgnore
    public synchronized List<AppMode> getAppModeList()
    {
        if(modesCache == null)
        {
            modesCache = AppMode.listFromCsvString(getAppModes());
        }
        return modesCache;
    }

    public void setAppModeList(@Nonnull List<AppMode> appModes)
    {
        setAppModes(AppMode.stringFromList(appModes));
    }

    /**
     * Query Track with all associations*/
    public static Track fetchTrackEager(long id)
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        String queryString = "from Track track inner join fetch track.trackImage left join fetch track.actionRegions ar left join fetch ar.video where track.id = :id";
        Query query = session.createQuery(queryString);
        query.setParameter("id", id);
        try
        {
            return (Track) query.getSingleResult();
        }
        catch (NoResultException e)
        {
            return null;
        }
    }

    public static class NameComparator implements Comparator<Track>
    {
        @Override
        public int compare(Track o1, Track o2)
        {
            return o2.getName().compareTo(o1.getName());
        }
    }
}
