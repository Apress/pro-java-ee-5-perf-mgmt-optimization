package com.javasrc.instrument;

// Import the Java classes
import java.util.*;

// Import the JDOM classes
import org.jdom.*;

/**
 * Singleton class that records transactions
 */
public class Instrumentor 
{
    /**
     * Maps request ids to a Stack (LinkedList) of method calls
     */
    private static Map requestStacks = new HashMap( 100 );

    /**
     * Maps request ids to request names
     */
    private static Map requestToIdMap = new HashMap( 100 );

    /**                         
     * Maps request names to RequestInfos
     */
    private static Map requests = new TreeMap();

    private static long startTime;
    private static long endTime;
    private static boolean instrumenting = false;

    public static void start()
    {
        startTime = System.currentTimeMillis();
        requestStacks.clear();
        requestToIdMap.clear();
        requests.clear();
        instrumenting = true;
    }

    public static void stop()
    {
        endTime = System.currentTimeMillis();
        instrumenting = false;
    }

    public static boolean isInstrumenting()
    {
        return instrumenting;
    }


    /**
     * Returns an id for the specified request name
     */
    public static String getId( String req )
    {
        return req + "-" + System.currentTimeMillis();
    }

    /**
     * Marks the start of a request
     */
    public static void startRequest( String id, String requestName )
    {
        // Only work if we are instrumenting
        if( !instrumenting )
        {
            return;
        }
        System.out.println( "Starting request: " + id + ", " + requestName );
        if( !requests.containsKey( requestName ) )
        {
            RequestInfo request = new RequestInfo( requestName );
            requests.put( requestName, request );
        }
        requestToIdMap.put( id, requestName );
    }

    /**
     * Marks the end of a request
     */
    public static void endRequest( String id )
    {
        // Only work if we are instrumenting
        if( !instrumenting )
        {
            return;
        }
        System.out.println( "Ending request: " + id );
        // Get the root element for this request
        LinkedList requestStack = ( LinkedList )requestStacks.get( id );
        MethodInfo root = ( MethodInfo )requestStack.removeLast();

        System.out.println( "ROOT:" + root );

        // See if we already have the request
        String requestName = ( String )requestToIdMap.get( id );
        System.out.println( "\tRequest Name: " + requestName );
        RequestInfo request = null;
        if( requests.containsKey( requestName ) )
        {
            // Found the request
            System.out.println( "Found the request..." );
            request = ( RequestInfo )requests.get( requestName );
            request.addRequest( root );
        }
        else
        {
            System.out.println( "Could not find request: " + requestName );
            /*
            // New request
            System.out.println( "New request..." );
            request = new RequestInfo( requestName );
            request.setRoot( root ); 
            requests.put( requestName, request );
            */
        }
    }

    /**
     * Marks the start of a method
     */
    public static void startMethod( String id, String qualifiedName )
    {
        // Only work if we are instrumenting
        if( !instrumenting )
        {
            return;
        }

        System.out.println( "Starting method: " + id + ", " + qualifiedName );
        // Get the Stack for this id
        LinkedList stack = null;
        if( requestStacks.containsKey( id ) )
        {
            stack = ( LinkedList )requestStacks.get( id );
        }
        else
        {
            stack = new LinkedList();
            requestStacks.put( id, stack );
        }

        // Build the method info and add it to our stack
        MethodInfo method = new MethodInfo( qualifiedName );
        method.start();
        stack.add( method );
    }

    /**
     * Marks the end of a method
     */
    public static void endMethod( String id )
    {
        // Only work if we are instrumenting
        if( !instrumenting )
        {
            return;
        }

        System.out.println( "Ending method: " + id );
        // Get the stack for this method
        LinkedList stack = ( LinkedList )requestStacks.get( id );

        // Get the last method executed
        MethodInfo method = ( MethodInfo )stack.removeLast();

        // Tell the method that it has completed
        method.end();

        // Add this method's info to its parent method
        if( stack.size() == 0 )
        {
            // Top of the stack; push it back on for endRequest to handle
            stack.addLast( method );
        }
        else
        {
            MethodInfo parent = ( MethodInfo )stack.getLast();
            parent.addSubMethod( method );
        }
    }

    public static Element toXML()
    {
        Element report = new Element( "instrumentation-report" );
        report.setAttribute( "request-count", Integer.toString( requests.size() ) );
        if( requests.size() == 0 )
        {
            return report;
        }
            
        report.setAttribute( "start-time", Long.toString( startTime ) );
        report.setAttribute( "end-time", Long.toString( endTime ) );
        report.setAttribute( "session-length", Long.toString( endTime - startTime ) );
        Element requestsElement = new Element( "requests" );
        for( Iterator i = requests.keySet().iterator(); i.hasNext(); )
        {
            String requestName = ( String )i.next();
            RequestInfo requestInfo = ( RequestInfo )requests.get( requestName );
            requestsElement.addContent( requestInfo.toXML() );
        }
        report.addContent( requestsElement );
        return report;
    }
}

