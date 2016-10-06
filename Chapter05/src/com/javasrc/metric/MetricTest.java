package com.javasrc.metric;

import junit.framework.TestCase;
import java.util.*;

public class MetricTest extends TestCase
{
    private Metric sampleHeap;

    protected void setUp()
    {
        this.sampleHeap = new Metric( "Test Metric", "Value/Min/Max", "megabytes" );
        double heapValue = 100.0;
        double heapMin = 50.0;
        double heapMax = 150.0;
        for( int i=0; i<10; i++ )
        {
            DataPoint dp = new DataPoint( new Date(), heapValue, heapMin, heapMax );
            this.sampleHeap.addDataPoint( dp );
            try
            {
                Thread.sleep( 50 );
            }
            catch( Exception e )
            {
            }

            // Update the heap values 
            heapMin -= 1.0;
            heapMax += 1.0;
            heapValue += 1.0;
        }

        // Debug: show datapoints
        /*
        System.out.println( "DataPoints" );
        Set s = this.sampleHeap.getDataPoints();
        for( Iterator i=s.iterator(); i.hasNext(); )
        {
            DataPoint dp = ( DataPoint )i.next();
            System.out.println( "\t" + dp );
        }
        System.out.println( "Standard Deviation: " + this.sampleHeap.getStandardDeviation() );
        System.out.println( "Variance: " + this.sampleHeap.getVariance() );
        */
    }

    public void testMin()
    {
        assertEquals( 41.0, this.sampleHeap.getMin(), 0.001 );
    }

    public void testMax()
    {
        assertEquals( 159.0, this.sampleHeap.getMax(), 0.001 );
    }

    public void testAve()
    {
        assertEquals( 104.5, this.sampleHeap.getAve(), 0.001 );
    }

    public void testMaxRange()
    {
        assertEquals( 118.0, this.sampleHeap.getMaxRange(), 0.001 );
    }
    
    public void testRange()
    {
        assertEquals( 118.0, this.sampleHeap.getRange(), 0.001 );
    }

    public void testSD()
    {
        assertEquals( 3.03, this.sampleHeap.getStandardDeviation(), 0.01 );
    }

    public void testVariance()
    {
        assertEquals( 9.17, this.sampleHeap.getVariance(), 0.01 );
    }

    public void testDataPointCount()
    {
        assertEquals( 10, this.sampleHeap.getDataPoints().size() );
    }
}
