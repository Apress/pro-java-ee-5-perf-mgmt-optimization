package com.javasrc.instrument.test.handlers;

import com.javasrc.instrument.Instrumentor;

public class BusinessProcess
{
    public void execute( String iid )
    {
        Instrumentor.startMethod( iid, "com.javasrc.instrument.test.handlers.BusinessProcess.execute()" );

        // Business logic
        try
        {
            Thread.sleep( 300 );
        }
        catch( Exception e ) 
        {
        }

        Instrumentor.endMethod( iid );
    }
}
