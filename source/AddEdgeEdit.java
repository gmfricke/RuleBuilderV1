/*
 * AddEdgeEdit.java
 *
 * Created on April 10, 2006, 10:10 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author matthew
 */
public class AddEdgeEdit extends Object implements Edit 
{
    Edge edge; // The wdiget that took the edit
    Connectable start;
    Connectable end;
    
    boolean undone = false;
    boolean redone = false;
       
    /** Creates a new instance of AddEdgeEdit */
    public AddEdgeEdit( Edge edge, Connectable start, Connectable end ) 
    {
        this.start = start;
        this.end = end;
        this.edge = edge;
    }
    
    public String getDescription() 
    {
        return "Add " + edge.getClass().getName();
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
        WidgetPanel panel = edge.getContainingPanel();
        
        if ( panel.getAllComponents().indexOf( start ) == -1 
             || panel.getAllComponents().indexOf( end ) == -1 )
        {
            panel.displayError( "Redo Failed","Could not redo edge addition because one of the original endpoints has been deleted.");
            return;
        }
        
        // Clear previous mouse offsets
        edge.resetOffsets();
        
        edge.link( start, end );
        panel.addEdge( edge );
        
        redone = true;
    }
    
    public void setType(String type) {
    }
    
    public void undo() 
    {
        // Containers need special handling since we dont want to remove the componets
        // inside
        
        edge.getContainingPanel().removeWidget( edge );
        
        undone = true;
    }
    
    public Widget getWidget()
    {
        return edge;
    }
}
