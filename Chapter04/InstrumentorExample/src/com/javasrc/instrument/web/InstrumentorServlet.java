package com.javasrc.instrument.web;

// Import the Servlet classes
import javax.servlet.*;
import javax.servlet.http.*;

// Import the Java classes
import java.util.*;
import java.io.*;

// Import the JDOM classes
import org.jdom.*;
import org.jdom.output.*;

// Import instrumention class
import com.javasrc.instrument.Instrumentor;

public class InstrumentorServlet extends HttpServlet
{
    public void service( HttpServletRequest req, HttpServletResponse res ) throws ServletException
    {
        try
        {
            // The command controls the action of this Servlet
            String command = req.getParameter( "cmd" );
            if( command == null ) command = "none";

            // The format controls the return format: html or xml
            String format = req.getParameter( "format" );
            if( format == null ) format = "html";
            boolean xml = format.equalsIgnoreCase( "xml" );
            
            String status = "Please make a selection";

            if( command.equalsIgnoreCase( "report" ) )
            {
                if( Instrumentor.isInstrumenting() )
                {
                    status = "Instrumentation is running, cannot generate a report until you stop instrumentation";
                }
                else
                {
                    // Convert the output of the report to an XML String
                    XMLOutputter outputter = new XMLOutputter( "\t", true );
                    status = outputter.outputString( Instrumentor.toXML() );

                    if( !xml )
                    {
                        status = xmlToHtml( status );
                    }
                }
            }
            else if( command.equalsIgnoreCase( "start" ) )
            {
                Instrumentor.start();
                status = "Instrumentor started";
                if( xml )
                {
                    status = "<status>" + status + "</status>";
                }
            }
            else if( command.equalsIgnoreCase( "stop" ) )
            {
                Instrumentor.stop();
                status = "Instrumentor stopped";
                if( xml )
                {
                    status = "<status>" + status + "</status>";
                }
            }

            // Update the instrumentation status 
            String instrumentationStatus = "Not Running";
            if( Instrumentor.isInstrumenting() )
            {
                instrumentationStatus = "Running";
            }

            if( xml )
            {
                PrintWriter out = res.getWriter();
                out.println( status );
                out.flush();
            }
            else
            {
                req.setAttribute( "instrumentation-status", instrumentationStatus );
                req.setAttribute( "status", status );
                RequestDispatcher rd = req.getRequestDispatcher( "instrument.jsp" );
                rd.forward( req, res );
            }

        }
        catch( Exception e )
        {
            e.printStackTrace();
            throw new ServletException( e );
        }
    }

    private String xmlToHtml( String xml )
    {
        StringBuffer sb = new StringBuffer( xml );
        int index = sb.indexOf( "<" );
        while( index != -1 )
        {
            sb.replace( index, index+1, "&lt;" );
            index = sb.indexOf( "<", index + 3 );
        }
        index = sb.indexOf( ">" );
        while( index != -1 )
        {
            sb.replace( index, index+1, "&gt;" );
            index = sb.indexOf( ">", index + 3 );
        }
        return sb.toString();
    }
}
