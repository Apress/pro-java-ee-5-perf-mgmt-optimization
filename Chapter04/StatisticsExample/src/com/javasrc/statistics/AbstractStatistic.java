package com.javasrc.statistics;

import javax.management.j2ee.statistics.*;

public abstract class AbstractStatistic implements Statistic
{
    protected String name;
    protected String description;
    protected String unit;
    protected long startTime;
    protected long lastSampleTime;

    public AbstractStatistic()
    {
    }

    public AbstractStatistic( String name,
                              String description,
                              String unit,
                              long startTime,
                              long lastSampleTime )
    {
        this.name = name;
        this.description = description;
        this.unit = unit;
        this.startTime = startTime;
        this.lastSampleTime = lastSampleTime;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getUnit()
    {
        return this.unit;
    }

    public void setUnit( String unit )
    {
        this.unit = unit;
    }

    public long getStartTime()
    {
        return this.startTime;
    }
    
    public void setStartTime( long startTime )
    {
        this.startTime = startTime;
    }

    public long getLastSampleTime()
    {
        return this.lastSampleTime;
    }

    public void setLastSampleTime( long lastSampleTime )
    {
        this.lastSampleTime = lastSampleTime;
    }
}
