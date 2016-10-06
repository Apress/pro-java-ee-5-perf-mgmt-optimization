package com.javasrc.instrument;

import java.util.*;

// Import the JDOM classes
import org.jdom.*;

public class RequestInfo
{
    private String request;
    private MethodInfo root;

    public RequestInfo( String request )
    {
        this.request = request;
    }

    public void addRequest( MethodInfo newRequest )
    {
        if( this.root == null )
        {
            // This is the first instance of this request, save it
            this.root = newRequest;
        }
        else
        {
            // Add this call to the request 
            this.root.addCall( newRequest );
        }
    }

    public Element toXML()
    {
        Element requestElement = new Element( "request" );
        requestElement.setAttribute( "name", request );
        requestElement.setAttribute( "ave-time", Long.toString( root.getAverage() ) );
        requestElement.setAttribute( "min-time", Long.toString( root.getMin() ) );
        requestElement.setAttribute( "max-time", Long.toString( root.getMax() ) );
        requestElement.setAttribute( "call-count", Integer.toString( root.getCallCount() ) );
        requestElement.addContent( root.toXML() );
        return requestElement;
    }
}
