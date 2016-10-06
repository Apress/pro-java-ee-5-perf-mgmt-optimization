package com.javasrc.tuning.jboss.jmx;

import javax.management.*;
import java.io.ObjectInputStream;
import java.util.Set;
import java.util.HashSet;
import java.rmi.RemoteException;

import com.javasrc.tuning.jmx.*;

// Import the JBoss classes
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.ejb.*;
import org.jboss.invocation.*;

/**
 * Because the MBeanServer in JBoss is a remote object, it is implemented by the RMIAdaptor
 * class; this class provides the functionality of the MBeanServer interface, but it does
 * not implement it - the Java constructs forbid adding exceptions to method declarations
 * so JBoss did not have any choice. The JBossMBeanServer class wraps calls to the 
 * RMIAdaptor and catches RemoteExceptions - it returns an appropriate null value if there
 * is a RemoteException
 */
public class JBossMBeanServer extends AbstractMBeanServer
{
    private RMIAdaptor rmiAdapter;

    public JBossMBeanServer( RMIAdaptor rmiAdapter )
    {
        this.rmiAdapter = rmiAdapter;
    }

    public Set queryNames(ObjectName objectname, QueryExp queryexp) 
    {
        try
        {
            return this.rmiAdapter.queryNames( objectname, queryexp );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            return new HashSet();
        }
    }

    public MBeanInfo getMBeanInfo(ObjectName objectname) throws InstanceNotFoundException,IntrospectionException,ReflectionException 
    {
        try
        {
            return this.rmiAdapter.getMBeanInfo( objectname );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            return null;
        }
    }

    public Object getAttribute(ObjectName objectname, String s) throws MBeanException,AttributeNotFoundException,InstanceNotFoundException,ReflectionException 
    {
        try
        {
            return this.rmiAdapter.getAttribute( objectname, s );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            return null;
        }
    }
}
