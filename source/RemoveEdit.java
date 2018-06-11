/*
 * AddEdit.java
 *
 * Created on December 5th, 2005, 11:55 AM
 */

import java.beans.*;
import java.io.Serializable;

/**
 * @author matthew
 */
public class RemoveEdit extends Object implements Edit 
{
    Widget widget; // The wdiget that took the edit
    boolean undone = false;
    boolean redone = false;
   
    int x = 0;
    int y = 0;
    
    public RemoveEdit(Widget widget, int x, int y)
    {
        this.widget = widget;
        this.x = x;
        this.y = y;
    }
    
    public String getDescription() 
    {
        return "Remove " + widget.getClass().getName();
    }    
   
    public boolean isReDone() 
    {
        return redone;
    }    
    
    public boolean isUnDone() 
    {
        return undone;
    }
    
    public void redo() 
    {
        redone = true;
     
        // Containers need special handling since we dont want to remove the componets
        // inside
        if ( widget instanceof BioContainer )
        {
            widget.getContainingPanel().removeContainerOnly( (BioContainer)widget );
        }
        else
        {
            widget.getContainingPanel().removeWidget( widget );
        }
      
    }
    
    public void setType(String type) {
    }
    
    public void undo() 
    {
        // Clear previous mouse offsets
        widget.resetOffsets();
        
        WidgetPanel panel = widget.getContainingPanel();
        panel.addWidgetToLocation( widget, x, y );
        
        undone = true;
    }
    
    public Widget getWidget()
    {
        return widget;
    }
}
