package com.javasrc.instrument.test;

import com.javasrc.instrument.Instrumentor;
import com.javasrc.instrument.test.handlers.*;
import com.javasrc.instrument.test.authentication.*;

public class Controller
{
    private BusinessProcess bp = new BusinessProcess();
    private Authentication auth = new Authentication();

    public void handle( String iid, String command )
    {
        Instrumentor.startMethod( iid, "com.javasrc.instrument.test.Controller.handle( String )" );
        try
        {
            // Business logic
            try
            {
                Thread.sleep( 100 );
            }
            catch( Exception e ) 
            {
            }

            if( auth.isValidUser( iid, "me" ) )
            {
                bp.execute(iid);
            }
        }
        finally
        {
            Instrumentor.endMethod( iid );
        }
    }
}
