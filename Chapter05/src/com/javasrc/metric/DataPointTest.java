package com.javasrc.metric;

import junit.framework.TestCase;
import java.util.*;

/**
 * Tests the core functionality of a DataPoint
 */
public class DataPointTest extends TestCase
{
    /**
     * Maintains our reference DataPoint
     */
    private DataPoint dp;

    /**
     * Create a DataPoint for use in this test
     */
    protected void setUp()
    {
        dp = new DataPoint( new Date(), 5.0, 1.0, 10.0 );
    }

    /**
     * Clean up: do nothing for now
     */
    protected void tearDown()
    {
    }

    /**
     * Test the range of the DataPoint
     */
    public void testRange()
    {
        assertEquals( 9.0, dp.getRange(), 0.001 );
    }

    /**
     * See if the DataPoint scales properly
     */
    public void testScale()
    {
        dp.scale( 10.0 );
        assertEquals( 50.0, dp.getValue(), 0.001 );
        assertEquals( 10.0, dp.getMin(), 0.001 );
        assertEquals( 100.0, dp.getMax(), 0.001 );
    }

    /**
     * Try to add a new DataPoint to our existing one
     */
    public void testAdd()
    {
        DataPoint other = new DataPoint( new Date(), 4.0, 0.5, 20.0 );
        dp.add( other );
        assertEquals( 9.0, dp.getValue(), 0.001 );
        assertEquals( 0.5, dp.getMin(), 0.001 );
        assertEquals( 20.0, dp.getMax(), 0.001 );
    }

    /**
     * Test the compare functionality of our DataPoint to ensure that
     * when we construct Sets of DataPoints that they are properly ordered
     */
    public void testCompareTo()
    {
        try
        {
            // Sleep for 100ms so that we can be sure that the time of 
            // the new datapoint is later than the first
            Thread.sleep( 100 );
        }
        catch( Exception e )
        {
        }

        // Construct a new DataPoint
        DataPoint other = new DataPoint( new Date(), 4.0, 0.5, 20.0 );

        // Should return -1 because other occurs after dp
        int result = dp.compareTo( other );
        assertEquals( -1, result );

        // Should return 1 because dp occurs before other
        result = other.compareTo( dp );
        assertEquals( 1, result );

        // Should return 0 because dp == dp
        result = dp.compareTo( dp );
        assertEquals( 0, result );
    }
}
