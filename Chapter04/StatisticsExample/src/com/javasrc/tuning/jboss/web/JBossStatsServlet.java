package com.javasrc.tuning.jboss.web;

// Import Servlet classes
import javax.servlet.*;
import javax.servlet.http.*;

// Import JNDI classes
import javax.naming.*;

// Import JDOM classes
import org.jdom.*;
import org.jdom.output.*;

// Import the Java classes
import java.util.*;
import javax.management.*;

// Import the JBoss classes
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;

// Import the tuning classes
import com.javasrc.tuning.web.*;
import com.javasrc.tuning.jmx.*;
import com.javasrc.tuning.jboss.jmx.JBossMBeanServer;

public class JBossStatsServlet extends AbstractStatsServlet
{
    /**
     * Classes extending this Servlet are responsible for locating and returning
     * an MBeanServer instance. This instance is used to preload object names and for
     * managing state access.
     */
    public MBeanServer getMBeanServer() 
    {
        try
        {
            InitialContext ic = new InitialContext();
            RMIAdaptor rmiAdaptor = (RMIAdaptor) ic.lookup("jmx/rmi/RMIAdaptor");
            return new JBossMBeanServer( rmiAdaptor );
        }
        catch( Exception e )
        {
            return null;
        }
    }

    /**
     * This is the main focus point of the Application Server specific Servlet classes;
     * though the getPerformanceRoot() method you will build an XML document that you
     * want to return to the caller
     */
    public Element getPerformanceRoot(MBeanServer server, Map objectNames) 
    {
        return new Element( "jboss-stats" );
    }
}

