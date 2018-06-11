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
public class AddEdit extends Object implements Edit 
{
    Widget widget; // The wdiget that took the edit
    boolean undone = false;
    boolean redone = false;
   
    int x = 0;
    int y = 0;
    
    public AddEdit(Widget widget, int x, int y)
    {
        this.widget = widget;
        this.x = x;
        this.y = y;
    }
     
    public String getDescription() 
    {
        return "Add " + widget.getClass().getName();
    }    
   
    public Widget getWidget()
    {
        return widget;
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
        if ( widget instanceof Edge )
        {
            widget.getContainingPanel().displayWarning("Internal Error","Edge encountered in AddEdit:redo.\nEdge redo/undo should be handled by the AddEdgeEdit class.");
            //redone = true;
            return;
        }
        
        // Clear previous mouse offsets
        widget.resetOffsets();
        
        WidgetPanel panel = widget.getContainingPanel();
        
        panel.addWidgetToLocation( widget, x, y );
        redone = true;
    }
    
    public void setType(String type) {
    }
    
    public void undo() 
    {
        // Containers need special handling since we dont want to remove the componets
        // inside
        if ( widget instanceof Edge )
        {
            widget.getContainingPanel().displayWarning("Internal Error","Edge encountered in class AddEdit.\nEdge redo\\undo should be handled by the AddEdgeEdit class.\nContact support at support@bionetgen.com");
            //redone = true;
            return;
        }
        
        if ( widget instanceof BioContainer )
        {
            widget.getContainingPanel().removeContainerOnly( (BioContainer)widget );
        }
        else
        {
            widget.getContainingPanel().removeWidget( widget );
        }
        
        undone = true;
    }
    
}
