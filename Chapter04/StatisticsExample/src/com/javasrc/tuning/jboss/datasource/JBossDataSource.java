package com.javasrc.tuning.jboss.datasource;

import java.io.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import com.javasrc.tuning.datasource.*;

public class JBossDataSource extends URLDataSource
{
    public JBossDataSource() throws DataSourceException
    {
    }

    public JBossDataSource( Element datasource ) throws DataSourceException
    {
        super( datasource );
    }
}
