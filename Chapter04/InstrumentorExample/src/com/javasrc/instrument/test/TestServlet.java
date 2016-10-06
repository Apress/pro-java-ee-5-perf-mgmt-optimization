package com.javasrc.instrument.test;

// Import the Servlet classes
import javax.servlet.*;
import javax.servlet.http.*;

// Import the Java classes
import java.io.*;

// Import instrumention class
import com.javasrc.instrument.Instrumentor;

public class TestServlet extends HttpServlet
{
    private boolean bool = false;
    public void service( HttpServletRequest req, HttpServletResponse res )
    {
        // Start the request 
        String requestName = req.getRequestURL().toString();
        String iid = Instrumentor.getId( requestName );
        Instrumentor.startRequest( iid, requestName );
        Instrumentor.startMethod( iid, "com.javasrc.instrument.test.TestServlet.service( HttpServletRequest, HttpServletResponse )" );

        // Business logic
        try
        {
            Thread.sleep( 100 );
            if( bool )
            {
                doNothing( iid );
            }
            else
            {
                doLessThanNothing( iid );
            }
            Controller c = new Controller();
            c.handle( iid, "something" );

            bool = !bool;
            PrintWriter out = res.getWriter();
            out.println( "<html><head><title>Test Servlet</head><body>test, test, test...</body></html>" );
            out.flush();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }

        // End the request
        Instrumentor.endMethod( iid );
        Instrumentor.endRequest( iid );
    }

    private void doNothing( String iid )
    {
        Instrumentor.startMethod( iid, "com.javasrc.instrument.test.TestServlet.doNothing()" );

        // Business logic
        try
        {
            Thread.sleep( 1000 );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }

        Instrumentor.endMethod( iid );
    }

    private void doLessThanNothing( String iid )
    {
        Instrumentor.startMethod( iid, "com.javasrc.instrument.test.TestServlet.doLessThanNothing()" );

        // Business logic
        try
        {
            Thread.sleep( 1000 );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }

        Instrumentor.endMethod( iid );
    }
}
