package com.javasrc.instrument.test.authentication;

import com.javasrc.instrument.Instrumentor;

public class Authentication
{
    public boolean isValidUser( String iid, String username )
    {
        Instrumentor.startMethod( iid, "com.javasrc.instrument.test.authentication.Authentication.isValidUser()" );
        try
        {
            // Business logic
            try
            {
                Thread.sleep( 200 );
            }
            catch( Exception e ) 
            {
            }
            return true;
        }
        finally
        {
            Instrumentor.endMethod( iid );
        }
    }
}
