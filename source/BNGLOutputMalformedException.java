/*
 * BNGLInputMalformedException.java
 *
 * Created on September 13, 2005, 11:17 PM
 */

import java.beans.*;
import java.io.Serializable;

/**
 * @author matthew
 */
public class BNGLOutputMalformedException extends Exception implements Serializable 
{
    private Exception hiddenException_;
    public BNGLOutputMalformedException(String error)
    {
       super(error);
    }
 
}   