package com.javasrc.metric;

import junit.framework.Test;
import junit.framework.TestSuite;

public class MetricTestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTestSuite( DataPointTest.class );
        suite.addTestSuite( MetricTest.class );
        return suite;
    }
}
