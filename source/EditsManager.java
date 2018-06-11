/*
 * EditManager.java
 *
 * Created on November 30, 2005, 4:19 PM by Matthew Fricke
 *
 * Provides an interface to the datastructure which holds edit objects.
 * EditManager provides two main public methods: undo and redo.
 *
 *  The information in this class does not need to be saved in the BNG file.
 */

import java.beans.*;
import java.io.Serializable;

import java.util.*; // For Stack

/**
 * @author matthew 
 */
public class EditsManager extends Object implements Serializable {
    
    Stack undo_edits = new Stack();
    Stack redo_edits = new Stack();
    
    public EditsManager() 
    {
         
    }
    
    public String getUndoDescription()
    {
        Edit edit = (Edit)undo_edits.peek();
        return edit.getDescription();
    }
    
    public String getRedoDescription()
    {
        Edit edit = (Edit)redo_edits.peek();
        return edit.getDescription();
    }
    
    public void addEdit( Edit edit )
    {
        edit.getWidget().getContainingPanel().getTheGUI().setSaveNeeded( true );
        
        // Handle edge edit special case transparently here
        Widget widget = edit.getWidget();
        
        if ( widget instanceof Edge )
        {
            Edge e = (Edge)widget;
            
            if ( edit instanceof RemoveEdit )
            {
                edit = new RemoveEdgeEdit( e, e.getStart(), e.getEnd() );
            }
            else if ( edit instanceof AddEdit )
            {
                edit = new AddEdgeEdit( e, e.getStart(), e.getEnd() );
            }
        }
            
        redo_edits.removeAllElements(); // Client preference that all redos become
        //invalid after a new edit
        undo_edits.push( edit );
    }
    
   public void undo() 
    {
       Edit edit = (Edit)undo_edits.pop();
       edit.getWidget().getContainingPanel().getTheGUI().setSaveNeeded( true );
        
        edit.undo();
        redo_edits.push( edit );
    }
    
   public void redo() 
    {
       Edit edit = (Edit)redo_edits.pop();
       edit.getWidget().getContainingPanel().getTheGUI().setSaveNeeded( true );
        
        edit.redo();
        undo_edits.push( edit );
    }
}
