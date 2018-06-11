/*
 * Edit.java
 *
 * Created on Nov 30, 2005, 4:26 PM
 *
 * This class contains information about actions which changed the BNG document
 * and information about how to undo the action. Edit objects are maintained by the
 * EditManager object.
 */

import java.beans.*;
import java.io.Serializable;

/**
 * @author matthew
 */
public interface Edit 
{ 
   
    public String getDescription();
    
    public Widget getWidget();
     
    public void setType( String type );
    
    public void undo();
    
    public void redo();
    
    public boolean isReDone();
    
    public boolean isUnDone();
}
