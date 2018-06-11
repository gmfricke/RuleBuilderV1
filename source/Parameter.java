/*
 * Parameter.java
 *
 * Created on November 2, 2005, 11:02 AM
 */

import java.beans.*;
import java.io.Serializable;

/**
 * @author matthew
 */
public class Parameter extends Object implements Serializable 
{
    private static final long serialVersionUID = 1;
    
    private String key;
    private String value;
    
    Parameter( String key, String value )
    {
        this.key = key;
        this.value = value;
    }
    
    public String getKey()
    {
        return key;
    }
    
    public String getValue()
    {
        return value;
    }
    
    public void setKey( String key )
    {
        this.key = key;
    }
    
    public void setValue( String value )
    {
        this.value = value;
    }
}
