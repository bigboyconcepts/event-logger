package com.newtecsolutions.floorball.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

/**
 * Created by pedja on 7/4/17 7:43 AM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
@Entity
public class CampTrainer extends HibernateModel
{
    public static final long CAMP_TRAINER_ID = 1;

    private File instructions;

    @OneToOne(fetch = FetchType.EAGER)
    public File getInstructions()
    {
        return instructions;
    }

    public void setInstructions(File instructions)
    {
        this.instructions = instructions;
    }
}
