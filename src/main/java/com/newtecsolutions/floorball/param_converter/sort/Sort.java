package com.newtecsolutions.floorball.param_converter.sort;

import com.newtecsolutions.floorball.utils.HibernateUtil;

/**
 * Created by pedja on 6/26/17 3:07 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class Sort
{
    public enum Order
    {
        DESC, ASC
    }
    private String field;
    private Order order;

    public Sort()
    {
    }

    public String getField()
    {
        return field;
    }

    public void setField(String field)
    {
        this.field = field;
    }

    public Order getOrder()
    {
        return order;
    }

    public void setOrder(Order order)
    {
        this.order = order;
    }

    /**
     * Create 'sort by' part of the query including 'sort by'.
     * @return example: 'sort by username DESC'*/
    public String createSortQueryPart(Class<?> modelClass)
    {
        boolean hasField = HibernateUtil.modelHasField(modelClass, field);
        if(!hasField)
            return "";
        StringBuilder builder = new StringBuilder();
        builder.append(" order by ").append(field).append(" ");
        if(order != null)
            builder.append(order.toString());
        builder.append(" ");
        return builder.toString();
    }
}
