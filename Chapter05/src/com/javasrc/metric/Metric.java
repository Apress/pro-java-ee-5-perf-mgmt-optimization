package com.javasrc.metric;

// Import the Java classes
import java.io.*;
import java.util.*;

// Import the JDOM classes
import org.jdom.*;

/**
 * Defines a PAS Metric node; contains a Set of datapoints
 */
public class Metric implements Serializable
{
    public static final int FUNCTION_UNKNOWN = -1;
    public static final int FUNCTION_VALUE = 1;
    public static final int FUNCTION_TOTAL = 2;
    public static final int FUNCTION_VALUE_MIN_MAX = 3;
    public static final int FUNCTION_AVG_MIN_MAX = 4;
    public static final int FUNCTION_PERCENT_MIN_MAX = 5;

    public static final int UNIT_UNKNOWN = -1;
    public static final int UNIT_COUNT = 1;
    public static final int UNIT_COUNT_PER_SECOND = 2;
    public static final int UNIT_THOUSANDTHS_PER_SECOND = 3;
    public static final int UNIT_MEGABYTES = 4;
    public static final int UNIT_GIGABYTES = 5;
    public static final int UNIT_PERCENT = 6;

    /**
     * The name of this metric
     */
    private String name;

    /**
     * The function of this metric
     */
    private int function;

    /**
     * The unit of this metric
     */
    private int unit;

    /**
     * An ordered set of datapoint; ordered by datapoint time
     */
    private Set datapoints = new TreeSet();

    private double min = -1.0;
    private double max = -1.0;
    private double ave = -1.0;
    private double maxRange = -1.0;
    private double aveRange = -1.0;
    private double variance = -1.0;
    private double standardDeviation = -1.0;

    /**
     * Creates a new Metric from a PAS metric node
     */
    public Metric( Element node )
    {
        // Get our attributes
        this.name = node.getAttributeValue( "name" );
        this.function = getFunctionValue( node.getAttributeValue( "function" ) );
        this.unit = getUnitValue( node.getAttributeValue( "unit" ) );

        // Get our datapoints
        List dps = node.getChildren( "data-point" );
        for( Iterator i=dps.iterator(); i.hasNext(); )
        {
            Element dp = ( Element )i.next();
            this.datapoints.add( new DataPoint( dp ) );
        }
    }

    public Metric( String name, String function, String unit )
    {
        this.name = name;
        this.function = getFunctionValue( function );
        this.unit = getUnitValue( unit );
    }

    public void addDataPoint( DataPoint dp )
    {
        this.datapoints.add( dp );
    }

    public void addDataPoint( Date time, double value, double min, double max )
    {
        this.datapoints.add( new DataPoint( time, value, min, max ) );
    }

    public void addDataPoint( Date time, double value )
    {
        this.datapoints.add( new DataPoint( time, value ) );
    }

    /**
     * Translates a function string to a function value
     */
    public static int getFunctionValue( String functionStr )
    {
        if( functionStr.equalsIgnoreCase( "value" ) )
        {
            return Metric.FUNCTION_VALUE;
        }
        else if( functionStr.equalsIgnoreCase( "total" ) )
        {
            return Metric.FUNCTION_TOTAL;
        }
        else if( functionStr.equalsIgnoreCase( "Value/Min/Max" ) )
        {
            return Metric.FUNCTION_VALUE_MIN_MAX;
        }
        else if( functionStr.equalsIgnoreCase( "Avg/Min/Max" ) )
        {
            return Metric.FUNCTION_AVG_MIN_MAX;
        }
        else if( functionStr.equalsIgnoreCase( "Percent/Min/Max" ) )
        {
            return Metric.FUNCTION_PERCENT_MIN_MAX;
        }
        else
        {
            return Metric.FUNCTION_UNKNOWN;
        }
    }

    /**
     * Translates a unit string to a unit value
     */
    public static int getUnitValue( String unitStr )
    {
        if( unitStr.equalsIgnoreCase( "count" ) )
        {
            return Metric.UNIT_COUNT;
        }
        else if( unitStr.equalsIgnoreCase( "count/second" ) )
        {
            return Metric.UNIT_COUNT_PER_SECOND;
        }
        else if( unitStr.equalsIgnoreCase( "thousandths/second" ) )
        {
            return Metric.UNIT_THOUSANDTHS_PER_SECOND;
        }
        else if( unitStr.equalsIgnoreCase( "megabytes" ) )
        {
            return Metric.UNIT_MEGABYTES;
        }
        else if( unitStr.equalsIgnoreCase( "gigabytes" ) )
        {
            return Metric.UNIT_GIGABYTES;
        }
        else if( unitStr.equalsIgnoreCase( "percent" ) )
        {
            return Metric.UNIT_PERCENT;
        }
        else
        {
            return Metric.UNIT_UNKNOWN;
        }
    }

    /**
     * Return the name of this metric
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Returns the unit of this metric
     */
    public int getUnit()
    {
        return this.unit;
    }

    /**
     * Returns the function of this metric
     */
    public int getFunction()
    {
        return this.function;
    }

    /**
     * Returns the Set of datapoints that this metric contains; this set
     * is ordered by the time that the datapoint occurred
     */
    public Set getDataPoints()
    {
        return this.datapoints;
    }

    public void setDataPoints( Set datapoints )
    {
        this.datapoints = datapoints;
    }

    public double getMin()
    {
        if( this.min == -1 )
        {
            generateStats();
        }
        if( this.min == -1 )
        {
            System.out.println( "###### Minimum is -1 for " + this.name );
        }
        return this.min;
    }

    public double getMax()
    {
        if( this.max == -1 )
        {
            generateStats();
        }
        return this.max;
    }

    public double getAve()
    {
        if( this.ave == -1 )
        {
            generateStats();
        }
        return this.ave;
    }

    public double getMaxRange()
    {
        if( this.maxRange == -1 )
        {
            generateStats();
        }
        return this.maxRange;
    }

    public double getAveRange()
    {
        if( this.aveRange == -1 )
        {
            generateStats();
        }
        return this.aveRange;
    }

    public double getRange()
    {
        if( this.min == -1 || this.max == -1 )
        {
            generateStats();
        }
        return this.max - this.min;
    }

    public double getVariance()
    {
        if( this.variance == -1 )
        {
            generateStats();
        }
        return this.variance;
    }

    public double getStandardDeviation()
    {
        if( this.standardDeviation == -1 )
        {
            generateStats();
        }
        return this.standardDeviation;
    }

    private void generateStats()
    {
        double total = 0.0;
        double totalRange = 0.0;
        int count = 0;
        for( Iterator i=this.datapoints.iterator(); i.hasNext(); )
        {
            DataPoint dp = ( DataPoint )i.next();
            if( max == -1 || dp.getMax() > max )
            {
                max = dp.getMax();
            }
            if( ( min == -1 || dp.getMin() < min ) && ( dp.getMin() >= 0 ) )
            {
                min = dp.getMin();
            }
            if( maxRange == -1 || dp.getRange() > maxRange )
            {
                maxRange = dp.getRange();
            }
            if( dp.getValue() >= 0 )
            {
                totalRange += dp.getRange();
                total += dp.getValue();
                count++;
            }
        }
        ave = total / count;
        aveRange = totalRange / count;

        // Compute the variance
        double vTotal = 0.0;
        for( Iterator i=this.datapoints.iterator(); i.hasNext(); )
        {
            DataPoint dp = ( DataPoint )i.next();
            if( dp.getValue() >= 0 )
            {
                double thisV = dp.getValue() - ave;
                thisV *= thisV;
                vTotal += thisV;
            }
        }
        this.variance = vTotal / ( count - 1 );

        // Compute the standard deviation
        this.standardDeviation = Math.sqrt( this.variance );
    }

    /**
     * Multiplies each of its datapoint's value, min, and max by the specified
     * multiplier
     */
    public void scale( double multiplier )
    {
        for( Iterator i=this.datapoints.iterator(); i.hasNext(); )
        {
            DataPoint dp = ( DataPoint )i.next();
            dp.scale( multiplier );
        }
    }

    public void addToMetric( Metric add )
    {
        for( Iterator i=this.datapoints.iterator(), j=add.getDataPoints().iterator(); i.hasNext(); )
        {
            DataPoint dpMine = ( DataPoint )i.next();
            DataPoint dpTheirs = ( DataPoint )j.next();
            dpMine.add( dpTheirs ); 
        }
    }
}

