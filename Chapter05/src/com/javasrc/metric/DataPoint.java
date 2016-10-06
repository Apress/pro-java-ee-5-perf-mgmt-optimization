package com.javasrc.metric;

import java.io.*;
import java.util.*;
import org.jdom.*;

/**
 * Represents a datapoint; this is either a single value or a value with
 * minimum and maximum values defining its range along with the time that the
 * datapoint was gathered
 */
public class DataPoint implements Serializable, Comparable
{
    /**
     * The time that this datapoint was gathered
     */
    private Date time;

    /**
     * The value of this datapoint
     */
    private double value;

    /**
     * If this datapoint defines a range, this is the minimum value during
     * the timeperiod in which it was gathered
     */
    private double min;

    /**
     * If this datapoint defines a range, this is the maximum value during
     * the timeperiod in which it was gathered
     */
    private double max;

    /**
     * Creates a new datapoint from the specified XML node
     */
    public DataPoint( Element dp )
    {
        this.time = new Date( JDOMUtils.getLongAttribute( dp, "time", 0 ) );
        this.value = JDOMUtils.getDoubleAttribute( dp, "value", -1 );
        this.min = JDOMUtils.getDoubleAttribute( dp, "top-value", -1 );
        if( this.min == -1 ) this.min = this.value;
        this.max = JDOMUtils.getDoubleAttribute( dp, "bottom-value", -1 );
        if( this.max == -1 ) this.max = this.value;

        // Sanity check - some metrics from PAS switch these nodes...
        if( this.min > this.max )
        {
            double tmp = this.min;
            this.min = this.max;
            this.max = tmp;
        }
    }

    public DataPoint( Date time, double value, double min, double max )
    {
        this.time = time;
        this.value = value;
        this.min = min;
        this.max = max;
    }

    public DataPoint( Date time, double value )
    {
        this.time = time;
        this.value = value;
    }

    /**
     * Returns the time that this datapoint was gathered
     */
    public Date getTime()
    {
        return this.time;
    }

    /**
     * Returns the value of this datapoint
     */
    public double getValue()
    {
        return this.value;
    }

    /**
     * Returns the minimum value of this datapoint during the collection time period
     */
    public double getMin()
    {
        return this.min;
    }

    public void setMin( double min )
    {
        this.min = min;
    }

    /**
     * Returns the maximum value of this datapoint during the collection time period
     */
    public double getMax()
    {
        return this.max;
    }

    public void setMax( double max )
    {
        this.max = max;
    }

    /**
     * Returns the range of this datapoint (max - min)
     */
    public double getRange()
    {
        return this.max - this.min;
    }
    
    /**
     * Performs comparison based off of the datapoint's time; this is used to
     * order datapoints in the order that they occurred
     */
    public int compareTo(Object o)
    {
        DataPoint other = ( DataPoint )o;
        long myTime = this.time.getTime();
        long theirTime = other.getTime().getTime();
        if( myTime < theirTime )
        {
            return -1;
        }
        else if( myTime > theirTime )
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }


    /**
     * Scales the value, min, and max values; multiplies them by the specified multiplier
     */
    public void scale( double multiplier )
    {
        this.value *= multiplier;
        this.min *= multiplier;
        this.max *= multiplier;
    }

    /**
     * Adds the "other" data point's values to this data point and scales the min/max to encompass
     * the range of both.
     */
    public void add( DataPoint other )
    {
        this.value += other.getValue();
        if( other.getMin() < this.min )
        {
            this.min = other.getMin();
        }
        if( other.getMax() > this.max )
        {
            this.max = other.getMax();
        }
    }


    public String toString()
    {
        return this.time + ", value=" + this.value + ", min=" + this.min + ", max=" + this.max;
    }
    
}
