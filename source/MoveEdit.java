/*
 * MoveEdit.java
 *
 * Created on November 30, 2005, 4:38 PM
 */

import java.beans.*;
import java.io.Serializable;

/**
 * @author matthew
 */
public class MoveEdit extends Object implements Edit 
{
    transient protected boolean debug_statements = true;
    
    Widget widget; // The wdiget that took the edit
    boolean undone = false;
    boolean redone = false;
    
    int undo_x = 0;
    int undo_y = 0;
    
    int redo_x = 0;
    int redo_y = 0;
    
    public MoveEdit( Widget widget, int original_x, int original_y )
    {
        this.widget = widget;
        this.undo_x = original_x;
        this.undo_y = original_y;
    }
    
    public String getDescription() 
    {
        return "Move " + widget.getClass().getName();
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
      
        undo_x = widget.getX();
        undo_y = widget.getY();
        
        if (debug_statements) System.out.println("Redo move: storing old position ("+ undo_x +"," + undo_y + ")" );
        if (debug_statements) System.out.println("Redo move: moving widget to ("+ redo_x +"," + redo_y + ")" );
        
        // Clear previous mouse offsets
        widget.resetOffsets();
        widget.updateLocation( redo_x, redo_y, false );
        redone = true;
    }
    
    public void setType(String type) {
    }
    
    public void undo() 
    {
        redo_x = widget.getX();
        redo_y = widget.getY();
        
        if (debug_statements) System.out.println("Redo move: storing old position ("+ redo_x +"," + redo_y + ")" );
        if (debug_statements) System.out.println("Undo move: moving widget to ("+ undo_x +"," + undo_y + ")" );
        
        // Clear previous mouse offsets
        widget.resetOffsets();
        widget.updateLocation( undo_x, undo_y, false );
        undone = true;
    }
    
    public Widget getWidget()
    {
        return widget;
    }
}
