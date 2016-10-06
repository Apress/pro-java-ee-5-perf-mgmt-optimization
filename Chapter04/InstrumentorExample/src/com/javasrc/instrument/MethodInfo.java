package com.javasrc.instrument;

import org.jdom.*;
import java.util.*;

/**
 * Stores information about a method and its sub methods
 */
public class MethodInfo
{
    /**
     * This method's class name
     */
    private String className;

    /**
     * This method's name
     */
    private String methodName;

    /**
     * The total time spent in this method
     */
    private long totalTime;

    /**
     * The number of times this method was called
     */
    private int callCount;

    /**
     * The minimum amount of time that this method was executed
     */
    private long minTime = -1;

    /**
     * The maximum amount of time that the method was executed
     */
    private long maxTime = -1;

    /**
     * Contains a list of all submethods that this method calls
     */
    private Map submethods = new TreeMap();

    /**
     * The start time of this method, used to compute method response time
     */
    private transient long startTime;

    /**
     * Creates a new MethodInfo
     * 
     * @param qualifiedName     The fully qualified name of the method
     */
    public MethodInfo( String qualifiedName )
    {
        int lastPeriod = qualifiedName.lastIndexOf( '.' );
        this.className = qualifiedName.substring( 0, lastPeriod );
        this.methodName = qualifiedName.substring( lastPeriod + 1 );
    }

    /**
     * The start of the method
     */
    public void start()
    {
        this.startTime = System.currentTimeMillis();
    }

    /**
     * The end of the method
     */
    public void end()
    {
        long endTime = System.currentTimeMillis();
        long methodTime = endTime - this.startTime;
        System.out.println( "Start time: " + startTime + ", end time: " + endTime + ", method time: " + methodTime );
        this.totalTime += methodTime;
        this.callCount++;

        if( this.minTime == -1 || methodTime < this.minTime )
        {
            this.minTime = methodTime;
        }

        if( this.maxTime == -1 || methodTime > this.maxTime )
        {
            this.maxTime = methodTime;
        }
    }

    /**
     * Returns the fully qualified method name
     */
    public String getMethodName()
    {
        return this.className + "." + this.methodName;
    }

    /**
     * Returns the call count of this method
     */
    public int getCallCount()
    {
        return this.callCount;
    }

    /**
     * Returns the average time that this method took to execute (in ms)
     */
    public long getAverage()
    {
        return ( long )( ( double )this.totalTime / ( double )this.callCount );
    }

    /**
     * Returns the minimum amount of time that this method took to execute (in ms)
     */
    public long getMin()
    {
        return ( long )this.minTime;
    }

    /**
     * Returns the maximum amount of time that this method took to execute (in ms)
     */
    public long getMax()
    {
        return ( long )this.maxTime;
    }

    /**
     * Returns the total time spent in this method
     */
    public long getTotalTime()
    {
        return this.totalTime;
    }

    /**
     * Returns all sub-methods
     */
    public Collection getSubMethods()
    {
        return this.submethods.values();
    }

    /**
     * Adds a submethod to this method
     */
    public void addSubMethod( MethodInfo method )
    {
        this.submethods.put( method.getMethodName(), method );
        //this.submethods.add( method );
    }

    /**
     * This method was called again, so add its information
     */
    public void addCall( MethodInfo newMethodCall )
    {
        // Add this method's info
        this.totalTime += newMethodCall.getTotalTime();
        this.callCount++;

        // Add the new method's submethods
        Collection newMethodCalls = newMethodCall.getSubMethods();
        for( Iterator i=newMethodCalls.iterator(); i.hasNext(); )
        {
            MethodInfo newMethod = ( MethodInfo )i.next();

            // Find this submethod
            if( this.submethods.containsKey( newMethod.getMethodName() ) )
            {
                // Add a new call to an existing method
                MethodInfo methodInfo = ( MethodInfo )this.submethods.get( newMethod.getMethodName() );
                methodInfo.addCall( newMethod );
            }
            else
            {
                // Add this method to our call tree
                this.addSubMethod( newMethod );
            }
        }
    }

    /**
     * Returns this method info as an XML node
     */
    public Element toXML()
    {
        // Build a method node
        long aveTime = this.getAverage();
        Element methodElement = new Element( "method" );
        methodElement.setAttribute( "name", this.methodName );
        methodElement.setAttribute( "class", this.className );
        methodElement.setAttribute( "ave-cumulative-time", Long.toString( aveTime ) );
        methodElement.setAttribute( "min-time", Long.toString( this.minTime ) );
        methodElement.setAttribute( "max-time", Long.toString( this.maxTime ) );
        methodElement.setAttribute( "total-time", Long.toString( this.totalTime ) );
        methodElement.setAttribute( "call-count", Integer.toString( this.callCount ) );

        // Add the sub methods
        long submethodTotalTime = 0;
        for( Iterator i=this.submethods.keySet().iterator(); i.hasNext(); )
        {
            String methodName = ( String )i.next();
            MethodInfo submethod = ( MethodInfo )this.submethods.get( methodName );
            methodElement.addContent( submethod.toXML() );
            submethodTotalTime += submethod.getTotalTime();
        }
        long totalExclusiveTime = this.totalTime - submethodTotalTime;
        long aveExclusiveTime = totalExclusiveTime / this.callCount;

        methodElement.setAttribute( "exclusive-ave-time", Long.toString( aveExclusiveTime ) );

        // Return the fully constructed method node
        return methodElement;
    }
}
