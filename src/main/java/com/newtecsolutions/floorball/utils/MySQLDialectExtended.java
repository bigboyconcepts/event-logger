package com.newtecsolutions.floorball.utils;

import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

/**
 * Created by pedja on 3/14/17 9:12 AM.
 * This class is part of the Floorball
 * Copyright © 2017 ${OWNER}
 */

@SuppressWarnings("unused")
public class MySQLDialectExtended extends MySQL5InnoDBDialect
{
    public MySQLDialectExtended()
    {
        super();
        //create new mysql functions
        //not really needed here, this was copied from other project
        registerFunction("srand", new SQLFunctionTemplate(StandardBasicTypes.FLOAT, "rand(?1)"));
        registerFunction("distance", new SQLFunctionTemplate(StandardBasicTypes.FLOAT, "(6371 * acos (cos (radians(?1)) * cos(radians(?3)) * cos(radians(?4) - radians(?2)) + sin(radians(?1)) * sin(radians(?3))))"));
    }
}
