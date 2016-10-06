package com.javasrc.tuning.jboss.web;

// Import Servlet classes
import javax.servlet.*;
import javax.servlet.http.*;

// Import JNDI classes
import javax.naming.*;

// Import JDOM classes
import org.jdom.*;
import org.jdom.output.*;

// Import the JBoss JMX classes
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.QueryExp;
import javax.naming.InitialContext;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.ejb.*;
import org.jboss.invocation.*;

// Import the Utility classes
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

public class StatsServlet extends HttpServlet
{
    private InitialContext ic;
    private ServletContext ctx = null;

    // Computation parameters
    private long now = 0l;
    private long lastSampleTime = 0l;
    //private Map lastExecuteQueueStatus = new TreeMap();
    private Element lastRequest = null;


    public void init() throws ServletException
    {
        try
        {
            // Load our contexts
            this.ctx = getServletContext();
            ic = new InitialContext();

            // See if we already have the ObjectName Map defined in the application object
            Map objectNames = ( Map )ctx.getAttribute( "object-names" );
            if( objectNames == null )
            {
                // Save our RMIServer and preload and save our object names
                RMIAdaptor server = (RMIAdaptor) ic.lookup("jmx/rmi/RMIAdaptor");
                objectNames = this.preloadObjectNames( server );
                ctx.setAttribute( "object-names", objectNames );
                ctx.setAttribute( "rmi-adaptor", server );
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    public void service( HttpServletRequest req, HttpServletResponse res ) throws ServletException 
    {
        try
        {
            //RMIAdaptor server = (RMIAdaptor) ic.lookup("jmx/rmi/RMIAdaptor");
            RMIAdaptor server = (RMIAdaptor)this.ctx.getAttribute( "rmi-adaptor" );
            String refresh = req.getParameter( "refresh" );
            Map objectNames = null;
            System.out.println( "refresh: " + refresh );
            if( refresh != null && refresh.equalsIgnoreCase( "true" ) )
            {
                objectNames = this.preloadObjectNames( server );
                System.out.println( "Refresh object map..." );
            }
            else
            {
                objectNames = ( Map )this.ctx.getAttribute( "object-names" );
            }
            this.now = System.currentTimeMillis();

            Element root = new Element( "jboss-tuning-stats" );
            root.addContent( getHeap() );
            root.addContent( getWeb( server, objectNames ) );
            root.addContent( getJDBC( server, objectNames ) );
            root.addContent( getEJBs( server, objectNames ) );
            root.addContent( getDetailedEJBs( server, objectNames ) );
            //root.addContent( getJTA( localHome ) );
            //root.addContent( getEntityBeans( localHome ) );

            // Test the classloader
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Element clElement = new Element( "class-loader" );
            clElement.setAttribute( "class", cl.getClass().getName() );
            root.addContent( clElement );

            // Dump the MBean info
            Element mbeans = new Element( "mbeans" );
            for( Iterator i=objectNames.keySet().iterator(); i.hasNext(); )
            {
                String key = ( String )i.next();
                Element domain = new Element( "domain" );
                domain.setAttribute( "name", key );
                Vector beans = ( Vector )objectNames.get( key );
                for( Iterator j=beans.iterator(); j.hasNext(); )
                {
                    ObjectName on = ( ObjectName )j.next();
                    Element bean = new Element( "mbean" );
                    bean.setAttribute( "name", on.getCanonicalName() );
                    domain.addContent( bean );
                }
                mbeans.addContent( domain );
            }
            root.addContent( mbeans );

            // Save our last sample time
            this.lastSampleTime = this.now; 

            // Save the last request
            this.lastRequest = root;

            // Output the XML document to the caller
            XMLOutputter out = new XMLOutputter( "\t", true );
            out.output( root, res.getOutputStream() );
        }
        catch( Exception e )
        {
            throw new ServletException( e );
        }
    }

    public Element getWeb( RMIAdaptor server, Map objectNames )
    {
        try
        {
            Vector web = ( Vector )objectNames.get( "jboss.web" );
            Element webElement  = new Element( "web" );

            for( Iterator i=web.iterator(); i.hasNext(); )
            {
                ObjectName name = ( ObjectName )i.next();
                String jettyValue = name.getKeyProperty( "Jetty" );
                String socketListenerValue = name.getKeyProperty( "SocketListener" );
                if( jettyValue != null && socketListenerValue != null )
                {
                    Element jetty = new Element( "jetty" );

                    ObjectName jettyName = new ObjectName( "jboss.web:Jetty=0,SocketListener=0" );
                    Integer maxThreads = ( Integer )server.getAttribute( jettyName, "maxThreads" );
                    Integer minThreads = ( Integer )server.getAttribute( jettyName, "minThreads" );
                    Integer threads = ( Integer )server.getAttribute( jettyName, "threads" );
                    Integer idleThreads = ( Integer )server.getAttribute( jettyName, "idleThreads" );
                    Boolean started = ( Boolean )server.getAttribute( jettyName, "started" );

                    jetty.setAttribute( "running", started.toString() );
                    jetty.setAttribute( "max-threads", maxThreads.toString() );
                    jetty.setAttribute( "min-threads", minThreads.toString() );
                    jetty.setAttribute( "current-threads", threads.toString() );
                    jetty.setAttribute( "threads-in-use", Integer.toString( threads.intValue() - idleThreads.intValue() ) );

                    webElement.addContent( jetty );
                }
            }
            return webElement;
        }
        catch( Exception e )
        {
            e.printStackTrace();
            return null;
        }
    }

    public Element getHeap()
    {
        // Get memory stats
        long freeMemory = Runtime.getRuntime().freeMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();

        // Convert bytes to megabytes
        freeMemory /= 1048576;
        totalMemory /= 1048576;

        // Build an XML Element
        Element heap = new Element( "heap" );
        heap.setAttribute( "total-memory", Long.toString( totalMemory ) );
        heap.setAttribute( "free-memory", Long.toString( freeMemory ) );
        heap.setAttribute( "used-memory", Long.toString( totalMemory - freeMemory ) );

        return heap;
    }

    private Map preloadObjectNames( RMIAdaptor server )
    {
        Map objectNames = new TreeMap();
        try
        {
            Set ons = server.queryNames( null, null );
            for( Iterator i=ons.iterator(); i.hasNext(); )
            {
                ObjectName name = ( ObjectName )i.next();
                String domain = name.getDomain();
                if( objectNames.containsKey( domain ) )
                {
                    // Load this domain's Vector from our map and 
                    // add this ObjectName to it
                    Vector v = ( Vector )objectNames.get( domain );
                    v.add( name );
                }
                else
                {
                    // This is a domain that we don't have yet, add it
                    // to our map
                    Vector v = new Vector();
                    v.add( name );
                    objectNames.put( domain, v );
                }
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        return objectNames;
    }

    public Element getJDBC( RMIAdaptor server, Map objectNames )
    {
        Element queryJDBC = new Element( "jdbc" );
        try
        {
            Vector connectors = ( Vector )objectNames.get( "jboss.jca" );
            for( Iterator i=connectors.iterator(); i.hasNext(); )
            {
                ObjectName name = ( ObjectName )i.next();
                String serviceName = name.getKeyProperty( "service" );
                if( serviceName != null && serviceName.equalsIgnoreCase( "LocalTxCM" ) )
                {
                    Element pool = new Element( "pool" );

                    pool.setAttribute( "name", name.getKeyProperty( "name" ) );
                    ObjectName managedPool = ( ObjectName )server.getAttribute( name, "ManagedConnectionPool" );
                    Integer maxSize = ( Integer )server.getAttribute( managedPool, "MaxSize" );
                    Integer minSize = ( Integer )server.getAttribute( managedPool, "MinSize" );

                    long availableConnectionCount = -1;
                    try
                    {
                        Long availableConnectionCountL = ( Long )server.getAttribute( managedPool, "AvailableConnectionCount" );
                        if( availableConnectionCountL != null )
                        {
                            availableConnectionCount = availableConnectionCountL.longValue();
                        }
                    }
                    catch( Exception e )
                    {
                        e.printStackTrace();
                    }

                    int connectionCount = -1;
                    try
                    {
                        //org.jboss.resource.connectionmanager.ManagedConnectionPool cp = 
                        //( org.jboss.resource.connectionmanager.ManagedConnectionPool )server.getAttribute( managedPool, "ManagedConnectionPool" );
                        //connectionCount = cp.getConnectionCount();
                        Integer connectionCountL = ( Integer )server.getAttribute( managedPool, "ConnectionCount" );
                        if( connectionCountL != null )
                        {
                            connectionCount = connectionCountL.intValue();
                        }
                    }
                    catch( Exception e )
                    {
                        e.printStackTrace();
                    }

                    pool.setAttribute( "max-size", maxSize.toString() );
                    pool.setAttribute( "min-size", minSize.toString() );
                    pool.setAttribute( "available-connection-count", Long.toString( availableConnectionCount ) );
                    pool.setAttribute( "connection-count", Integer.toString( connectionCount ) );

                    // Compute the connections in use
                    long connectionsInUse = -1;
                    if( availableConnectionCount != -1 )
                    {
                        if( connectionCount == 0 )
                        {
                            connectionsInUse = 0;
                        }
                        else
                        {
                            connectionsInUse = maxSize.longValue() - availableConnectionCount;
                        }
                    }
                    pool.setAttribute( "connections-in-use", Long.toString( connectionsInUse ) );

                    queryJDBC.addContent( pool );
                }
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        return queryJDBC;

    }

    public Element getEJBs( RMIAdaptor server, Map objectNames )
    {
        Element deployedEJBs = new Element( "ejbs" );
        try
        {
            Vector ejbNames = ( Vector )objectNames.get( "jboss.ejb" );
            for( Iterator i=ejbNames.iterator(); i.hasNext(); )
            {
                ObjectName name = ( ObjectName )i.next();
                String serviceName = name.getKeyProperty( "service" );
                if( serviceName != null && serviceName.equalsIgnoreCase( "EJBDeployer" ) )
                {
                    //Element deployerInfo = this.getMBeanInfo( server, name );
                    //deployedEJBs.addContent( deployerInfo );

                    Iterator iterator = ( Iterator )server.getAttribute( name, "DeployedApplications" ); 
                    while( iterator.hasNext() )
                    {
                        // Handle an EJB Module
                        Element bean = new Element( "ejb-module" );
                        DeploymentInfo info = ( DeploymentInfo )iterator.next();
                        ObjectName beanName = info.deployedObject;
                        bean.setAttribute( "name", beanName.getCanonicalName() );

                        // Get the EJBModule's MBeans
                        List mbeans = info.mbeans;
                        for( Iterator mbeanIterator = mbeans.iterator(); mbeanIterator.hasNext(); )
                        {
                            // Represents an individual Bean Container
                            ObjectName mbean = ( ObjectName )mbeanIterator.next();

                            // Get the bean info
                            Element beanChild = new Element( "bean" );
                            beanChild.setAttribute( "name", mbean.getKeyProperty( "jndiName" ) );
                            Element mbeanInfo = this.getMBeanInfo( server, mbean );
                            String className = mbeanInfo.getAttributeValue( "class-name" );
                            if( className.equalsIgnoreCase( "org.jboss.ejb.MessageDrivenContainer" ) )
                            {
                                beanChild.setAttribute( "type", "Message Driven Bean" ); 
                            }
                            else if( className.equalsIgnoreCase( "org.jboss.ejb.EntityContainer" ) )
                            {
                                beanChild.setAttribute( "type", "Entity Bean" ); 
                            }
                            else if( className.equalsIgnoreCase( "org.jboss.ejb.StatelessSessionContainer" ) )
                            {
                                beanChild.setAttribute( "type", "Stateless Session Bean" ); 
                            }
                            else if( className.equalsIgnoreCase( "org.jboss.ejb.StatefulSessionContainer" ) )
                            {
                                beanChild.setAttribute( "type", "Stateful Session Bean" ); 
                            }

                            bean.addContent( beanChild );
                        }
                        deployedEJBs.addContent( bean );
                    }
                }
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        return deployedEJBs;
    }

    public Element getDetailedEJBs( RMIAdaptor server, Map objectNames )
    {
        Element ejbs = new Element( "ejb-details" );

        // Map JNDI name to element that is of the form:
        // <ejb name="jndiName" bean-type="entity|...">
        //   <cache .../>
        //   <pool .../>
        // </ejb>
        Map ejbMap = new TreeMap();
        try
        {
            Vector ejbNames = ( Vector )objectNames.get( "jboss.j2ee" );
            for( Iterator i=ejbNames.iterator(); i.hasNext(); )
            {
                ObjectName name = ( ObjectName )i.next();
                String serviceName = name.getKeyProperty( "service" );
                if( serviceName != null && serviceName.equalsIgnoreCase( "EJB" ) )
                {
                    String jndiName = name.getKeyProperty( "jndiName" );
                    
                    // Lookup the element
                    Element beanElement;
                    if( ejbMap.containsKey( jndiName ) )
                    {
                        beanElement = ( Element )ejbMap.get( jndiName );
                    }
                    else
                    {
                        beanElement = new Element( "ejb" );
                        ejbMap.put( jndiName, beanElement );
                    }

                    // See what node we are building
                    String plugin = name.getKeyProperty( "plugin" );
                    if( plugin == null )
                    {
                        // ---------------------------------------------------
                        // Handle the general bean info:
                        // ---------------------------------------------------
                        beanElement.setAttribute( "name", jndiName );
                        Long createCount = ( Long )server.getAttribute( name, "CreateCount" );
                        Long removeCount = ( Long )server.getAttribute( name, "RemoveCount" );
                        beanElement.setAttribute( "create-count", createCount.toString() );
                        beanElement.setAttribute( "remove-count", removeCount.toString() );
                        
                        // Handle Invocation Statistics
                        Element invocationStats = new Element( "invocation-stats" );
                        InvocationStatistics stats = ( InvocationStatistics )server.getAttribute( name, "InvokeStats" );
                        Map statsMap = stats.getStats();
                        long totalInvocations = 0;
                        long totalTotalTime = 0;
                        for( Iterator ii=statsMap.keySet().iterator(); ii.hasNext(); )
                        {
                            Element methodElement = new Element( "method" );
                            Method m = ( Method )ii.next();
                            InvocationStatistics.TimeStatistic time = ( InvocationStatistics.TimeStatistic )statsMap.get( m );
                            methodElement.setAttribute( "name", m.getName() );
                            methodElement.setAttribute( "count", Long.toString( time.count ) );
                            methodElement.setAttribute( "min-time", Long.toString( time.minTime ) );
                            methodElement.setAttribute( "max-time", Long.toString( time.maxTime ) );
                            methodElement.setAttribute( "total-time", Long.toString( time.totalTime ) );
                            methodElement.setAttribute( "ave-time", Double.toString( ( double )time.totalTime / ( double )time.count ) );
                            invocationStats.addContent( methodElement );
                            totalInvocations += time.count;
                            totalTotalTime += time.totalTime;
                        }

                        // Roll up averages for the bean
                        invocationStats.setAttribute( "ave-time", Double.toString( ( double )totalTotalTime / ( double )totalInvocations ) );

                        // Add the invocation statistics to the bean
                        beanElement.addContent( invocationStats );


                        // ---------------------------------------------------
                        // Handle the container specific stuff
                        // ---------------------------------------------------
                        String containerName = ( String )server.getAttribute( name, "Name" );
                        if( containerName.equalsIgnoreCase( "EntityContainer" ) )
                        {
                            // Handle Entity Bean
                            beanElement.setAttribute( "bean-type", "entity-bean" );
                            Long cacheSize = ( Long )server.getAttribute( name, "CacheSize" );
                            beanElement.setAttribute( "cache-size", cacheSize.toString() );
                        }
                        else if( containerName.equalsIgnoreCase( "StatelessSessionContainer" ) )
                        {
                            // Stateless Session Bean
                            beanElement.setAttribute( "bean-type", "stateless-session-bean" );

                        }
                        else if( containerName.equalsIgnoreCase( "MessageDrivenContainer" ) )
                        {
                            // Handle Message Driven Bean
                            beanElement.setAttribute( "bean-type", "message-driven-bean" );
                            Long msgCount = ( Long )server.getAttribute( name, "MessageCount" );
                            beanElement.setAttribute( "message-count", msgCount.toString() );
                        }
                        else if( containerName.equalsIgnoreCase( "StatefulSessionContainer" ) )
                        {
                            // Handle Stateful Bean
                            beanElement.setAttribute( "bean-type", "stateful-session-bean" );
                            Long cacheCount = ( Long )server.getAttribute( name, "CacheCount" );
                            beanElement.setAttribute( "cache-count", cacheCount.toString() );
                        }
                    }
                    else if( plugin.equalsIgnoreCase( "cache" ) )
                    {
                        Element cache = new Element( "cache" );
                        Long passivatedCount = ( Long )server.getAttribute( name, "PassivatedCount" );
                        Long cacheSize = ( Long )server.getAttribute( name, "CacheSize" );
                        cache.setAttribute( "passivated-count", passivatedCount.toString() );
                        cache.setAttribute( "cache-size", cacheSize.toString() );
                        beanElement.addContent( cache );
                    }
                    else if( plugin.equalsIgnoreCase( "pool" ) )
                    {
                        Element pool = new Element( "pool" );
                        Integer poolSize = ( Integer )server.getAttribute( name, "CurrentSize" );
                        Integer maxSize = ( Integer )server.getAttribute( name, "MaxSize" );
                        String poolClassName = ( String )server.getAttribute( name, "Name" );
                        pool.setAttribute( "class", poolClassName );
                        pool.setAttribute( "max-size", maxSize.toString() );
                        pool.setAttribute( "current-size", poolSize.toString() );
                        beanElement.addContent( pool );
                    }
                }
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        
        // Build ejb nodes from the map
        for( Iterator ii=ejbMap.keySet().iterator(); ii.hasNext(); )
        {
            String jndiName = ( String )ii.next();
            Element element = ( Element )ejbMap.get( jndiName );
            ejbs.addContent( element );
        }

        // Return the ejbs node
        return ejbs;
    }

    private Element getMBeanInfo( RMIAdaptor server, ObjectName mbean )
    {
        Element mbeanElement = new Element( "mbean" );
        try
        {
            MBeanInfo info = server.getMBeanInfo( mbean );

            // Get the general stuff
            mbeanElement.setAttribute( "class-name", info.getClassName() );
            mbeanElement.setAttribute( "description", info.getDescription() );

            // List the attributes
            MBeanAttributeInfo attributes[] = info.getAttributes();
            Element attributesElement = new Element( "attributes" );
            for( int i=0; i<attributes.length; i++ )
            {
                MBeanAttributeInfo attr = attributes[ i ];
                Element attributeElement = new Element( "attribute" );
                attributeElement.setAttribute( "name", attr.getName() );
                attributeElement.setAttribute( "desc", attr.getDescription() );
                attributeElement.setAttribute( "type", attr.getType() );
                String value = server.getAttribute( mbean, attr.getName() ).toString(); 
                attributeElement.setAttribute( "value", value );
                attributesElement.addContent( attributeElement );
            }
            mbeanElement.addContent( attributesElement );

            // List the operations
            MBeanOperationInfo[] operations = info.getOperations();
            Element operationsElement = new Element( "operations" );
            for( int i=0; i<operations.length; i++ )
            {
                MBeanOperationInfo operation = operations[ i ];
                Element operationElement = new Element( "operation" );
                operationElement.setAttribute( "name", operation.getName() );
                operationElement.setAttribute( "desc", operation.getDescription() );
                operationElement.setAttribute( "return-type", operation.getReturnType() );
                MBeanParameterInfo[] parameters = operation.getSignature();
                if( parameters.length > 0 )
                {
                    StringBuffer sb = new StringBuffer();
                    for( int j=0; j<parameters.length; j++ )
                    {
                        sb.append( parameters[ j ].getType() + " " + parameters[ j ].getName() + ", " ); 
                    }
                    operationElement.setAttribute( "parameters", sb.toString() ); 
                }
                operationsElement.addContent( operationElement );
            }
            mbeanElement.addContent( operationsElement );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }

        return mbeanElement;
    }

    /*
    public Element getExecuteQueues( MBeanHome home )
    {
    Element eqs = new Element( "execute-queues" );
        Set s = home.getMBeansByType( "ExecuteQueueRuntime" );
        for( Iterator i=s.iterator(); i.hasNext(); )
        {
        Element eq = new Element( "execute-queue" );
            ExecuteQueueRuntimeMBean exeQ = ( ExecuteQueueRuntimeMBean )i.next();
            String eqName = exeQ.getName();
            eq.setAttribute( "name", eqName );
            int totalThreads = exeQ.getExecuteThreads().length;
        int idleThreads = exeQ.getExecuteThreadCurrentIdleCount();

        eq.setAttribute( "threads-total", Integer.toString( totalThreads ) );
        eq.setAttribute( "threads-in-use", Integer.toString( totalThreads - idleThreads ) );
        eq.setAttribute( "pending-requests", Integer.toString( exeQ.getPendingRequestCurrentCount() ) );

            int throughputNow = exeQ.getServicedRequestTotalCount();
            eq.setAttribute( "total-throughput", Integer.toString( throughputNow ) );

            // Compute the throughput per second
            int throughput = 0;
            if( !this.lastExecuteQueueStatus.isEmpty() )
            {
                Element eqNode = ( Element )this.lastExecuteQueueStatus.get( eqName );
                int lastThroughputValue = 0;
                if( eqNode != null )
                {
                    String lastThroughputValueStr = eqNode.getAttributeValue( "total-throughput" );
                    lastThroughputValue = Integer.parseInt( lastThroughputValueStr );
                    throughput = ( throughputNow - lastThroughputValue ) /
                                 ( int )( ( this.now - this.lastSampleTime ) / 1000 );
                }
            }
        eq.setAttribute( "throughput", Integer.toString( throughput ) );

        eqs.addContent( eq );

            // Save this node for our next invocation
            this.lastExecuteQueueStatus.remove( eqName );
            this.lastExecuteQueueStatus.put( eqName, eq ); 
        }

    return eqs;
    }

    public Element getJTA( MBeanHome home )
    {
    Element jtaElement = new Element( "jta" );
        Set s = home.getMBeansByType( "JTARuntime" );
        for( Iterator i=s.iterator(); i.hasNext(); )
        {
        try
        {
        JTARuntimeMBean jta = ( JTARuntimeMBean )i.next();
        TransactionNameRuntimeMBean[] txns = jta.getTransactionNameRuntimeMBeans();
        for( int j=0; j<txns.length; j++ )
        {
            TransactionNameRuntimeMBean txn = txns[ j ];
            jtaElement.setAttribute( "transactions-total", Long.toString( txn.getTransactionTotalCount() ) );
            
                    long transactionsCommitted = txn.getTransactionCommittedTotalCount();
            long appRollbacks = txn.getTransactionRolledBackAppTotalCount();
            long systemRollbacks = txn.getTransactionRolledBackSystemTotalCount();
            long timeoutRollbacks = txn.getTransactionRolledBackTimeoutTotalCount();
            long resourceRollbacks = txn.getTransactionRolledBackResourceTotalCount();

            jtaElement.setAttribute( "transactions-committed-total", Long.toString( transactionsCommitted  ) );
            jtaElement.setAttribute( "transactions-rolledback-total", Long.toString( txn.getTransactionRolledBackTotalCount() ) );
            jtaElement.setAttribute( "rollbacks-application-total", Long.toString( appRollbacks ) );
            jtaElement.setAttribute( "rollbacks-system-total", Long.toString( systemRollbacks ) );
            jtaElement.setAttribute( "rollbacks-timeout-total", Long.toString( timeoutRollbacks ) );
            jtaElement.setAttribute( "rollbacks-resource-total", Long.toString( resourceRollbacks ) );
            jtaElement.setAttribute( "heuristics-total", Long.toString( txn.getTransactionHeuristicsTotalCount() ) );
            jtaElement.setAttribute( "transactions-abandonded", Long.toString( txn.getTransactionAbandonedTotalCount() ) );
                    
                    if( this.lastRequest != null )
                    {
                        long sampleSeconds = ( this.now - this.lastSampleTime ) / 1000;
                        long transactionsPreviouslyCommitted = Long.parseLong( this.lastRequest.getChild( "jta" ).getAttributeValue( "transactions-committed-total" ) );
                        long transactionsCommittedPerSecond = ( transactionsCommitted - transactionsPreviouslyCommitted ) / sampleSeconds; 
                        jtaElement.setAttribute( "transactions-committed-per-second", Long.toString( transactionsCommittedPerSecond  ) );

                        long previousAppRollbacks = Long.parseLong( this.lastRequest.getChild( "jta" ).getAttributeValue( "rollbacks-application-total" ) );
                        long appRollbacksPerSecond = ( appRollbacks - previousAppRollbacks ) / sampleSeconds; 
                        jtaElement.setAttribute( "rollbacks-application-per-second", Long.toString( appRollbacksPerSecond  ) );

                        long previousSystemRollbacks = Long.parseLong( this.lastRequest.getChild( "jta" ).getAttributeValue( "rollbacks-system-total" ) );
                        long systemRollbacksPerSecond = ( systemRollbacks - previousSystemRollbacks ) / sampleSeconds; 
                        jtaElement.setAttribute( "rollbacks-system-per-second", Long.toString( systemRollbacksPerSecond  ) );
                        
                        long previousTimeoutRollbacks = Long.parseLong( this.lastRequest.getChild( "jta" ).getAttributeValue( "rollbacks-timeout-total" ) );
                        long timeoutRollbacksPerSecond = ( timeoutRollbacks - previousTimeoutRollbacks ) / sampleSeconds; 
                        jtaElement.setAttribute( "rollbacks-timeout-per-second", Long.toString( timeoutRollbacksPerSecond  ) );
                        
                        long previousResourceRollbacks = Long.parseLong( this.lastRequest.getChild( "jta" ).getAttributeValue( "rollbacks-resource-total" ) );
                        long resourceRollbacksPerSecond = ( resourceRollbacks - previousResourceRollbacks ) / sampleSeconds; 
                        jtaElement.setAttribute( "rollbacks-resource-per-second", Long.toString( resourceRollbacksPerSecond  ) );
                    }
                    else
                    {
                        jtaElement.setAttribute( "transactions-committed-per-second", "0" ); 
                        jtaElement.setAttribute( "rollbacks-application-per-second", "0" );
                        jtaElement.setAttribute( "rollbacks-system-per-second", "0" );
                        jtaElement.setAttribute( "rollbacks-timeout-per-second", "0" );
                        jtaElement.setAttribute( "rollbacks-resource-per-second", "0" );
                    }

        }
        }
        catch( Exception e )
        {
        e.printStackTrace();
        }
        }
    return jtaElement;
    }
    
    public Element getEntityBeans( MBeanHome home )
    {
        try
        {
            Element entityBeans = new Element( "entity-beans" );

            Set s = home.getMBeansByType( "EntityEJBRuntime" );
            for( Iterator i=s.iterator(); i.hasNext(); )
            {
                EntityEJBRuntimeMBean mbean = (EntityEJBRuntimeMBean)i.next();
                EJBCacheRuntimeMBean cache = mbean.getCacheRuntime();
                WebLogicObjectName objectName = mbean.getObjectName();

                String name = mbean.getObjectName().getName();
                long activationCount = cache.getActivationCount();
                long passivationCount = cache.getPassivationCount();
                int cacheCurrentCount = cache.getCachedBeansCurrentCount();
                long hitCount = cache.getCacheHitCount();
                long accessCount = cache.getCacheAccessCount();

                Element entityBean = new Element( "entity-bean" );
                entityBean.setAttribute( "name", name );
                entityBean.setAttribute( "activation-total", Long.toString( activationCount ) );
                entityBean.setAttribute( "passivation-total", Long.toString( passivationCount ) );
                entityBean.setAttribute( "cache-current-count", Integer.toString( cacheCurrentCount ) );
                entityBean.setAttribute( "cache-hit-count-total", Long.toString( hitCount ) );
                entityBean.setAttribute( "cache-access-count-total", Long.toString( accessCount ) );

                if( this.lastRequest != null )
                {
                    long sampleSeconds = ( this.now - this.lastSampleTime ) / 1000;

                    Map ebs = this.getEntityBeanNodeMap();
                    Element eb = ( Element )ebs.get( name );
                    long previousActivationCount = Long.parseLong( eb.getAttributeValue( "activation-total" ) );
                    long previousPassivationCount = Long.parseLong( eb.getAttributeValue( "passivation-total" ) );
                    long previousHitCount = Long.parseLong( eb.getAttributeValue( "cache-hit-count-total" ) );
                    long previousAccessCount = Long.parseLong( eb.getAttributeValue( "cache-access-count-total" ) );

                    long activationCountPerSecond = ( activationCount - previousActivationCount ) / sampleSeconds;
                    long passivationCountPerSecond = ( passivationCount - previousPassivationCount ) / sampleSeconds;
                    long hitCountPerSecond = ( hitCount - previousHitCount ) / sampleSeconds;
                    long accessCountPerSecond = ( accessCount - previousAccessCount ) / sampleSeconds;

                    entityBean.setAttribute( "activations-per-second", Long.toString( activationCountPerSecond ) );
                    entityBean.setAttribute( "passivations-per-second", Long.toString( passivationCountPerSecond  ) );
                    entityBean.setAttribute( "cache-hit-count-per-second", Long.toString( hitCountPerSecond ) );
                    entityBean.setAttribute( "cache-access-count-per-second", Long.toString( accessCountPerSecond ) );
                }
                else
                {
                    entityBean.setAttribute( "activations-per-second", "0" );
                    entityBean.setAttribute( "passivations-per-second", "0" );
                    entityBean.setAttribute( "cache-hit-count-per-second", "0" );
                    entityBean.setAttribute( "cache-access-count-per-second", "0" );
                }

                // Add this element to our document 
                entityBeans.addContent( entityBean );
            }

            return entityBeans;
        }
        catch( Exception e )
        {
            e.printStackTrace();
            return null;
        }

    }

    private Map getEntityBeanNodeMap()
    {
        try
        {
            Map map = new TreeMap();
            List entityBeanList = this.lastRequest.getChild( "entity-beans" ).getChildren( "entity-bean" ); 
            for( Iterator i=entityBeanList.iterator(); i.hasNext(); )
            {
                Element eb = ( Element )i.next();
                map.put( eb.getAttributeValue( "name" ), eb );
            }
            return map;
        }
        catch( Exception e )
        {
            e.printStackTrace();
            return null;
        }
    }
    */
}
