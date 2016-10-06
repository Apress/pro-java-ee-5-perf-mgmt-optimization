package com.javasrc.metric;

import org.jdom.*;
import org.jdom.input.*;

public class JDOMUtils
{
    private static SAXBuilder builder;

    public static Element getRootElement( String filename )
    {
        try
        {
            if( builder == null )
            {
                builder = new SAXBuilder();
            }
            Document doc = builder.build( filename );
            return doc.getRootElement();
        }
        catch( Exception ex )
        {
            ex.printStackTrace();
        }
        return null;
    }
    public static int getChildInt( Element e, String childName, int defaultValue )
    {
        try
        {
            String value = e.getChildTextTrim( childName );
            int intVal = Integer.parseInt( value );
            return intVal;
        }
        catch( Exception ex )
        {
        }
        return defaultValue;
    }
    
    public static long getChildLong( Element e, String childName, long defaultValue )
    {
        try
        {
            String value = e.getChildTextTrim( childName );
            long val = Long.parseLong( value );
            return val;
        }
        catch( Exception ex )
        {
        }
        return defaultValue;
    }

    public static float getChildFloat( Element e, String childName, float defaultValue )
    {
        try
        {
            String value = e.getChildTextTrim( childName );
            float floatVal = Float.parseFloat( value );
            return floatVal;
        }
        catch( Exception ex )
        {
        }
        return defaultValue;
    }

    public static double getChildDouble( Element e, String childName, double defaultValue )
    {
        try
        {
            String value = e.getChildTextTrim( childName );
            double doubleVal = Double.parseDouble( value );
            return doubleVal;
        }
        catch( Exception ex )
        {
        }
        return defaultValue;
    }
    
    public static long getLongAttribute( Element e, String attr, long defaultValue )
    {
        try
        {
            String value = e.getAttributeValue( attr );
            long longVal = Long.parseLong( value );
            return longVal;
        }
        catch( Exception ex )
        {
        }
        return defaultValue; 
    }

    public static int getIntAttribute( Element e, String attr, int defaultValue )
    {
        try
        {
            String value = e.getAttributeValue( attr );
            int intVal = Integer.parseInt( value );
            return intVal;
        }
        catch( Exception ex )
        {
        }
        return defaultValue; 
    }
    
    public static double getDoubleAttribute( Element e, String attr, double defaultValue )
    {
        try
        {
            String value = e.getAttributeValue( attr );
            double doubleVal = Double.parseDouble( value );
            return doubleVal;
        }
        catch( Exception ex )
        {
        }
        return defaultValue;
    }

    public static float getFloatAttribute( Element e, String attr, float defaultValue )
    {
        try
        {
            String value = e.getAttributeValue( attr );
            float floatVal = Float.parseFloat( value );
            return floatVal;
        }
        catch( Exception ex )
        {
        }
        return defaultValue;
    }
    
    public static boolean getBooleanAttribute( Element e, String attr, boolean defaultValue )
    {
        try
        {
            String value = e.getAttributeValue( attr );
            if( value.equalsIgnoreCase( "true" ) )
            {
                return true;
            }
            return false;
        }
        catch( Exception ex )
        {
        }
        return defaultValue;
    }

    public static int getInt( Element e, int defaultValue )
    {
        try
        {
            String value = e.getTextTrim();
            int intVal = Integer.parseInt( value );
            return intVal;
        }
        catch( Exception ex )
        {
        }
        return defaultValue;
    }
    
    public static float getFloat( Element e, float defaultValue )
    {
        try
        {
            String value = e.getTextTrim();
            float floatVal = Float.parseFloat( value );
            return floatVal;
        }
        catch( Exception ex )
        {
        }
        return defaultValue;
    }
}                 
