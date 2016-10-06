package com.javasrc.tuning.weblogic.web;

// Import JNDI classes
import javax.naming.*;

// Import JDOM classes
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

// Import the Java classes
import java.util.*;
import javax.management.*;
import javax.naming.*;

// Import the WebLogic JMX classes
import weblogic.jndi.Environment;
import weblogic.management.*;
import weblogic.management.runtime.*;
import weblogic.management.configuration.*;
import weblogic.management.descriptors.*;
import weblogic.management.descriptors.toplevel.*;
import weblogic.management.descriptors.weblogic.*;
//import weblogic.health.*;



// Import our base class
import com.javasrc.tuning.web.*;

public class WebLogicStatsServlet extends AbstractStatsServlet
{
    /**
     * Classes extending this Servlet are responsible for locating and returning
     * an MBeanServer instance. This instance is used to preload object names and for
     * managing state access.
     */
    public MBeanServer getMBeanServer()
    {
        // Load our initialization information
        String url = null;
        String username = null;
        String password = null;
        try
        {
            String config = getServletContext().getResource("/WEB-INF/xml/stats.xml").toString();
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build( config );
            Element root = doc.getRootElement();
            Element adminServer = root.getChild( "admin-server" );
            String port = adminServer.getAttributeValue( "port" );
            url = "t3://localhost:" + port;
            username = adminServer.getAttributeValue( "username" );
            password = adminServer.getAttributeValue( "password" );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }

        // Retrieve a reference to the MBeanServer
        MBeanHome localHome = ( MBeanHome )Helper.getAdminMBeanHome( username, password, url );
        return localHome.getMBeanServer();
    }

    /**
     * This is the main focus point of the Application Server specific Servlet classes;
     * though the getPerformanceRoot() method you will build an XML document that you
     * want to return to the caller
     */
    public Element getPerformanceRoot(MBeanServer server, Map objectNames)
    {
        Element root = new Element( "weblogic-tuning-stats" );

        // Build the document
        try
        {
            Element executeQueuesElement = new Element( "execute-queues" );
            int threadCount = 0;
            int threadsMin = 0;
            int threadsMax = 0;
            int threadsIncrease = 0;
            int queueLength = 0;
            List executeQueueConfigs = getObjectNames( objectNames, "examples", "ExecuteQueueConfig" );
            for( Iterator i=executeQueueConfigs.iterator(); i.hasNext(); )
            {
                ObjectName on = ( ObjectName )i.next();
                String name = on.getKeyProperty( "Name" );
                if( name.equalsIgnoreCase( "weblogic.kernel.Default" ) )
                {
                    javax.management.Attribute attribute = ( javax.management.Attribute )server.getAttribute( on, "ThreadCount" );
                    threadCount = Integer.parseInt( attribute.getValue().toString() );
                }
            }
            Element executeQueueElement = new Element( "execute-queue" );
            executeQueueElement.setAttribute( "name", "weblogic.kernel.Default" );
            executeQueueElement.setAttribute( "thread-count", Integer.toString( threadCount ) );
            executeQueuesElement.addContent( executeQueueElement );
            root.addContent( executeQueuesElement );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }






        // Return the document
        return root;
    }
}
