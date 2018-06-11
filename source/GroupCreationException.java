/*
 * SpeciesCreationException.java
 *
 * Created on August 25th, 2005, 6:40 PM
 */

import java.beans.*;
import java.io.Serializable;

/**
 * @author matthew 
 */
public class GroupCreationException extends Exception implements Serializable 
{
    private Exception hiddenException_;
    public GroupCreationException(String error)
    {
       super(error);
    }
 
}   