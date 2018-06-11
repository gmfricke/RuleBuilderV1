/*
 * ReactionPalette.java
 *
 * Created on September 25, 2005, 12:22 PM
 */

/**
 *C(c)
 * @author  matthew
 */

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import javax.swing.*;

import java.awt.*; // For graphical windowing tools
import java.awt.event.*; // For mouse interactions
import javax.swing.event.MouseInputAdapter;

import java.util.*; //For vector data structure

public class ReactionPalette extends WidgetPalette 
{
    
    private class MouseControl extends MouseInputAdapter
    {
	
        private WidgetPalette palette;
        
        public MouseControl(WidgetPalette palette) 
        {
           this.palette = palette;
        }
    
    // Handles the event of the user pressing down the mouse button.
    // Here the program determines which widget the mouse pointer was over a
    // when the button was pressed. If the widget pressed is a template then
    // a new widget is created and assigned to be the selected widget. If the
    // widget pressed is not a template then it is the selected widget.
    
	public void mousePressed(MouseEvent e)
	{
            // does nothing
            if (true) return;
            
            // Compensate for zoom and translation
            // getZoomedX(Y)Translation should always be 0 for WidgetPalettes
            int mouse_x = (int)(((e.getX()-getZoomedXTranslation())/getZoom()));
            int mouse_y = (int)(((e.getY()-getZoomedYTranslation())/getZoom()));    
            
            clearSelections();
            
	    // Determine which reaction rule was selected.
            
            Iterator i = rules.iterator();
	    while ( i.hasNext() )
		{
		    ReactionRule rr = (ReactionRule)i.next();
		    if ( rr.contains( mouse_x, mouse_y ) ) 
		    {
			if (debug_statements) System.out.println("Reaction Rule Pressed");
                       
			selected_reactionrule = rr;
                        
                        if ( e.isPopupTrigger() )
			    {
				selected_reactionrule.displayPopupMenu( e );
				return;
			    }
                        
			selected_reactionrule.setSelected(true);
		    }
                }
        }

    

    // Handles the event of a user dragging the mouse while holding
    // down the mouse button.
    public void mouseDragged(MouseEvent e)
	{    
        
            // does nothing
            if (true) return;
            
	   if (debug_statements) System.out.println("Mouse drag detected in ReactionRule Palette");
         
            JComponent c = (JComponent) e.getSource();
            TransferHandler th = c.getTransferHandler();
            if (th != null) 
            {
                th.exportAsDrag(c, e, DnDConstants.ACTION_MOVE );
            }
            else
            {
                displayError("Error in Drag 'n Drop of ReactionRule","ReactionRule Handler was NULL. Contact support at support@bionetgen.com");
            }
   
	}

    // Handles the event of a user releasing the mouse button.
    public void mouseReleased(MouseEvent e)
    {
        // does nothing
            if (true) return;
        
        if ( selected_reactionrule != null && e.isPopupTrigger() )
        {
            selected_reactionrule.displayPopupMenu( e );
            return;
	}
	
	
    }
       
     // This method is required by MouseListener.
     public void mouseMoved(MouseEvent e){}

     // These methods are required by MouseMotionListener.
     public void mouseClicked(MouseEvent e){}
     public void mouseExited(MouseEvent e){}
     public void mouseEntered(MouseEvent e){}

}

        
    // Serialization explicit version
    private static final long serialVersionUID = 1;

    // Transient so StateSaver wont try to serialize it
    transient private MouseControl local_mouse_control = new MouseControl( this );
    //ScrollControl scroll_control = new ScrollControl();
    
    /** Creates a new instance of SpeciesPanel */
    public ReactionPalette( GUI the_gui ) 
    {
        super( the_gui );
        removeMouseMotionListener( mouse_control );
        removeMouseListener(mouse_control);
        addMouseMotionListener(local_mouse_control);
	addMouseListener(local_mouse_control);

        area = new Dimension(0,0);
        
     	setBackground(Color.white);
    }
   
    public void actionPerformed( ActionEvent action )
    {  
        if ( action.getActionCommand().equals("Delete") )
	{
            if ( selected_reaction != null )
            {
                removeReaction( selected_reactionrule );
                return;
            }
        }
        
        super.actionPerformed( action );
    }
    
    // Template for addRule
    synchronized boolean addReaction( Reaction r )
    {
	if (debug_statements) System.out.println("Adding reaction to the reaction palette.");
        
	if ( rules.indexOf( r ) != -1 || r == null ) 
	    {
		return false;
	    }

	r.setContainingPanel(this);

        r.setSelected( false );
        
        if (debug_statements) System.out.println("Add reaction: reaction at " + r.getX() + "," + r.getY() );
        
        //positionElement( r );
        //compressDisplay();
        
        if (debug_statements) System.out.println("Add reaction: reaction now at " + r.getX() + "," + r.getY() );
        
	super.addReaction( r ); 
        
        compressDisplay();
        repaint();
        
        if (debug_statements) System.out.println("There are now " + reactions.size() + " reactions in this palette.");
        
	return true;
    }
     
   
    synchronized public void removeReaction(ReactionRule rr) 
    {
        if (debug_statements) System.out.println("Removing Reaction Rule in the Reaction Rule Palette");
        super.removeReactionRule( rr );
        
        compressDisplay();
        clearSelections();
        refreshAll();
        
        // Remove from model too
        getTheGUI().getModel().removeReactionRule( rr );
    }    
    
    synchronized public boolean pasteCopiedWidget( int x, int y ) 
    {
        Widget copied_widget = getTheGUI().copied_widget;
        
        if ( !(copied_widget instanceof ReactionRule) )
        {
            displayError("Error Pasting " + copied_widget.getClass().getName(),
            "Only Reaction Rules may be pasted into the Reaction Rule palette." );
            return false;
        }
        
        Reaction r = null;
        try
        {
            r = (Reaction)WidgetCloner.clone(copied_widget);
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        
        addReaction( r );
        return true;
    }
    
    public boolean cutSelectedWidget()
    {
                        copySelectedWidget();
                        removeReaction( selected_reactionrule );
                        compressDisplay();
                        clearSelections();
			refreshAll();
                        
                        return true;
    }

}
