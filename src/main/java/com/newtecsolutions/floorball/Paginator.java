package com.newtecsolutions.floorball;

import com.newtecsolutions.floorball.utils.ConfigManager;
import com.newtecsolutions.floorball.utils.HibernateUtil;

import org.hibernate.Session;

import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.Query;

/**
 * Created by pedja on 6/22/17 8:58 AM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 *
 * Handles returning results in pages
 */
public class Paginator<T>
{
    public static final int DEF_PER_PAGE = ConfigManager.getInstance().getInt(ConfigManager.CONFIG_DB_QUERY_DEF_PER_PAGE, 20);
    public static final int MAX_PER_PAGE = ConfigManager.getInstance().getInt(ConfigManager.CONFIG_DB_QUERY_MAX_PER_PAGE, 100);

    /**
     * This is hibernate model class, model of the database table we are querying*/
    private Class<? extends T> _class;

    public Paginator(Class<? extends T> _class)
    {
        this._class = _class;
    }

    /**
     * <pre>
     * Load next page
     * Use this also for first page
     * </pre>
     *
     * @param page page to load
     * @param perPage max results per page to return. If perPage < 1 value will be set to {@link #DEF_PER_PAGE} and if perPage > {@link #MAX_PER_PAGE} to {@link #MAX_PER_PAGE}
     * @param _query query to execute, Can be null. If null default query will be created, for example 'from Member'*/
    public Page<T> nextPage(int page, int perPage, @Nullable Query _query)
    {
        if (perPage < 1)
            perPage = DEF_PER_PAGE;
        if (perPage > MAX_PER_PAGE)
            perPage = MAX_PER_PAGE;
        if (page < 1)
            page = 1;

        int offset = (page - 1) * perPage + (page == 1 ? 0 : 1);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        Query query = _query != null ? _query : session.createQuery("from " + _class.getSimpleName());
        query.setFirstResult(offset);
        query.setMaxResults(perPage);

        return new Page<T>(query.getResultList(), perPage, page);
    }

    /**
     * Wrapper for single page with results*/
    public static class Page<T>
    {
        private List<T> items;
        private int perPage, page;

        public Page(List<T> items, int perPage, int page)
        {
            this.items = items;
            this.perPage = perPage;
            this.page = page;
        }

        public List<T> getItems()
        {
            return items;
        }

        public void setItems(List<T> items)
        {
            this.items = items;
        }

        public int getPerPage()
        {
            return perPage;
        }

        public void setPerPage(int perPage)
        {
            this.perPage = perPage;
        }

        public int getPage()
        {
            return page;
        }

        public void setPage(int page)
        {
            this.page = page;
        }
    }
}
